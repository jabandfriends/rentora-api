package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.MonthlyInvoice.Metadata.MonthlyInvoiceMetadataDto;
import com.rentora.api.model.dto.MonthlyInvoice.Response.MonthlyInvoiceResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.*;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.specifications.MonthlyInvoiceSpecification;
import com.rentora.api.specifications.UnitUtilitySpecification;
import com.rentora.api.specifications.UtilitySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyInvoiceService {
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final UnitServiceRepository unitServiceRepository;
    private final AdhocInvoiceRepository adhocInvoiceRepository;
    private final ContractRepository contractRepository;
    private final UnitUtilityRepository unitUtilityRepository;
    private final ApartmentRepository apartmentRepository;

    public Page<MonthlyInvoiceResponseDto> getAllMonthlyInvoice(Invoice.PaymentStatus paymentStatus, String unitName,String buildingName,
                                     UUID apartmentId, Pageable pageable){
        Specification<Invoice> specification = MonthlyInvoiceSpecification.hasApartmentId(apartmentId)
                .and(MonthlyInvoiceSpecification.hasBuildingName(buildingName)).and(MonthlyInvoiceSpecification.hasUnitName(unitName))
                .and(MonthlyInvoiceSpecification.hasPaymentStatus(paymentStatus));

        Page<Invoice> monthlyInvoices = invoiceRepository.findAll(specification,pageable);

        return monthlyInvoices.map(this::toMonthlyInvoiceResponseDto);
    }

    public MonthlyInvoiceMetadataDto getMonthlyInvoiceMetadata(UUID apartmentId){
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow(()->new ResourceNotFoundException("Apartment not found"));

        long totalMonthlyInvoices = invoiceRepository.countByApartmentId(apartmentId);
        long totalPaidMonthlyInvoices = invoiceRepository.countByApartmentAndPaymentStatus(apartment, Invoice.PaymentStatus.paid);
        long totalUnpaidMonthlyInvoices = invoiceRepository.countByApartmentAndPaymentStatus(apartment, Invoice.PaymentStatus.unpaid);
        long totalOverdueMonthlyInvoices = invoiceRepository.countByApartmentAndPaymentStatus(apartment, Invoice.PaymentStatus.overdue);

        return MonthlyInvoiceMetadataDto.builder().totalMonthlyInvoices(totalMonthlyInvoices)
                .totalPaidMonthlyInvoices(totalPaidMonthlyInvoices).totalUnpaidMonthlyInvoices(totalUnpaidMonthlyInvoices)
                .totalOverdueMonthlyInvoice(totalOverdueMonthlyInvoices).build();
    }

    public void createMonthlyInvoice(UserPrincipal admin, UUID unitId, Integer readingMonth, Integer readingYear, Integer paymentDueDays) {
        Invoice monthlyInvoice = new Invoice();
        //find current
        User currentAdmin = userRepository.findById(admin.getId()).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        // Get active contract
        Contract activeContract = contractRepository.findActiveContractByUnitId(unitId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        Apartment contractApartment = activeContract.getUnit().getFloor().getBuilding().getApartment();

        // Set billing date based on reading month/year
        LocalDate billStart = LocalDate.now()
                .withYear(readingYear)
                .withMonth(readingMonth)
                .withDayOfMonth(1);
        LocalDate billEnd = billStart.withDayOfMonth(billStart.lengthOfMonth());
        LocalDate dueDate = LocalDate.now().plusDays(paymentDueDays);

        // 3️Get latest utility readings (null-safe)
        UnitUtilities latestWaterMeter = getLatestUnitUtilitySafe(activeContract.getUnit().getId(), "water", billStart);
        UnitUtilities latestElectricMeter = getLatestUnitUtilitySafe(activeContract.getUnit().getId(), "electric", billStart);

        // Calculate utility amounts
        BigDecimal utilityAmount = calculateUtilityAmountSafe(latestWaterMeter, latestElectricMeter);

        // Calculate adhoc and unit service amounts
        BigDecimal totalAdhocAmount = adhocInvoiceRepository.findByUnit(activeContract.getUnit()).stream()
                .map(AdhocInvoice::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnitServiceAmount = unitServiceRepository.findAllByUnitId(activeContract.getUnit().getId()).stream()
                .map(UnitServiceEntity::getMonthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = (activeContract.getUtilitiesIncluded() ? utilityAmount : BigDecimal.ZERO)
                .add(totalAdhocAmount)
                .add(totalUnitServiceAmount);

        // Fill invoice fields
        monthlyInvoice.setApartment(contractApartment);
        monthlyInvoice.setUnit(activeContract.getUnit());
        monthlyInvoice.setContract(activeContract);
        monthlyInvoice.setTenant(activeContract.getTenant());
        monthlyInvoice.setFeesAmount(contractApartment.getLateFee());
        monthlyInvoice.setDiscountAmount(BigDecimal.ZERO);
        monthlyInvoice.setGeneratedByUser(currentAdmin);
        monthlyInvoice.setPaymentDueDate(dueDate);
        monthlyInvoice.setUtilAmount(utilityAmount);
        monthlyInvoice.setBillStart(billStart);
        monthlyInvoice.setBillEnd(billEnd);
        monthlyInvoice.setGenMonth(billStart);
        monthlyInvoice.setDueDate(dueDate);

        // Calculate rent based on contract type
        BigDecimal rentAmount = calculateRent(activeContract);
        monthlyInvoice.setRentAmount(rentAmount);

        totalAmount = totalAmount.add(rentAmount);
        monthlyInvoice.setTotalAmount(totalAmount);

        // Save invoice
        invoiceRepository.save(monthlyInvoice);

        log.info("Monthly invoice created for unit {} for month {}-{}", unitId, readingMonth, readingYear);
    }

    private UnitUtilities getLatestUnitUtilitySafe(UUID unitId, String utilityName, LocalDate usageMonth) {
        Specification<UnitUtilities> spec = UnitUtilitySpecification.hasUnitId(unitId)
                .and(UnitUtilitySpecification.hasUtilityName(utilityName)
                        .and(UnitUtilitySpecification.hasUsageMonth(usageMonth)));
        return unitUtilityRepository.findOne(spec).orElse(null);
    }

    private BigDecimal calculateUtilityAmountSafe(UnitUtilities waterMeter, UnitUtilities electricMeter) {
        return calculateSingleUtilitySafe(waterMeter).add(calculateSingleUtilitySafe(electricMeter));
    }

    private BigDecimal calculateSingleUtilitySafe(UnitUtilities meter) {
        if (meter == null || meter.getUtility() == null) return BigDecimal.ZERO;

        Utility utility = meter.getUtility();
        if (utility.getUtilityType() == Utility.UtilityType.fixed) {
            return utility.getFixedPrice() != null ? utility.getFixedPrice() : BigDecimal.ZERO;
        } else if (utility.getUtilityType() == Utility.UtilityType.meter) {
            return (utility.getUnitPrice() != null && meter.getUsageAmount() != null)
                    ? utility.getUnitPrice().multiply(meter.getUsageAmount())
                    : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    /** Rent calculation remains the same */
    private BigDecimal calculateRent(Contract contract) {
        if (contract.getRentalType() == Contract.RentalType.monthly) {
            return contract.getRentalPrice() != null ? contract.getRentalPrice() : BigDecimal.ZERO;
        } else if (contract.getRentalType() == Contract.RentalType.daily) {
            long days = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate()) + 1;
            return contract.getRentalPrice() != null ? contract.getRentalPrice().multiply(BigDecimal.valueOf(days)) : BigDecimal.ZERO;
        } else if (contract.getRentalType() == Contract.RentalType.yearly) {
            return BigDecimal.ZERO; // already paid
        }
        return BigDecimal.ZERO;
    }

    private MonthlyInvoiceResponseDto toMonthlyInvoiceResponseDto(Invoice invoice) {
        return MonthlyInvoiceResponseDto.builder().invoiceId(invoice.getId())
                .buildingName(invoice.getContract().getUnit().getFloor().getBuilding().getName())
                .paymentStatus(invoice.getPaymentStatus()).tenantName(invoice.getTenant().getFullName())
                .totalAmount(invoice.getTotalAmount()).unitName(invoice.getContract().getUnit().getUnitName()).build();
    }

}
