package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.mapper.AdhocInvoiceMapper;
import com.rentora.api.mapper.UnitServiceMapper;
import com.rentora.api.model.dto.MonthlyInvoice.Metadata.MonthlyInvoiceMetadataDto;
import com.rentora.api.model.dto.MonthlyInvoice.Response.MonthlyInvoiceDetailResponseDto;
import com.rentora.api.model.dto.MonthlyInvoice.Response.MonthlyInvoiceResponseDto;
import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitAdhocInvoice;
import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitServiceList;
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
import java.util.Optional;
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
    private final ApartmentPaymentRepository apartmentPaymentRepository;
    private final PaymentRepository paymentRepository;

    private final UnitServiceMapper unitServiceMapper;
    private final AdhocInvoiceMapper adhocInvoiceMapper;

    public Page<MonthlyInvoiceResponseDto> getAllMonthlyInvoice(LocalDate genMonth, Invoice.PaymentStatus paymentStatus, String unitName,String buildingName,
                                     UUID apartmentId, Pageable pageable){
        Specification<Invoice> specification = MonthlyInvoiceSpecification.hasApartmentId(apartmentId)
                .and(MonthlyInvoiceSpecification.hasBuildingName(buildingName)).and(MonthlyInvoiceSpecification.hasUnitName(unitName))
                .and(MonthlyInvoiceSpecification.hasPaymentStatus(paymentStatus)).and(MonthlyInvoiceSpecification.hasContractNotDaily())
                .and(MonthlyInvoiceSpecification.matchGenerationDate(genMonth));

        Page<Invoice> monthlyInvoices = invoiceRepository.findAll(specification,pageable);



        return monthlyInvoices.map(this::toMonthlyInvoiceResponseDto);
    }

    public List<MonthlyInvoiceDetailResponseDto> getAllMonthlyInvoiceDetail(UUID apartmentId,LocalDate genMonth){
        Specification<Invoice> specification = MonthlyInvoiceSpecification.hasApartmentId(apartmentId).and(MonthlyInvoiceSpecification.matchGenerationDate(genMonth));
        List<Invoice> monthlyInvoices = invoiceRepository.findAll(specification);

        return monthlyInvoices.stream().map(this::toMonthlyInvoiceDetailDto).toList();
    }

    public MonthlyInvoiceDetailResponseDto getMonthlyInvoiceDetail(String invoiceNumber){
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber).
                orElseThrow(() -> new ResourceNotFoundException("Invoice number " + invoiceNumber + " not found"));

        return toMonthlyInvoiceDetailDto(invoice);
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

    //monthly yearly monthly rental invoice
    public void createMonthlyInvoice(UserPrincipal admin, UUID unitId, LocalDate readingDate) {
        Invoice monthlyInvoice = new Invoice();
        // find current
        User currentAdmin = userRepository.findById(admin.getId()).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        // Get active contract
        Contract activeContract = contractRepository.findActiveContractByUnitId(unitId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        //filter contract type filter out daily contract
        if(activeContract.getRentalType().equals(Contract.RentalType.daily)) throw new BadRequestException("Daily Rental not allowed" +
                " to create monthly invoice");
        Apartment contractApartment = activeContract.getUnit().getFloor().getBuilding().getApartment();

        // Set billing date based on reading month/year
        LocalDate billStart = LocalDate.now()
                .withYear(readingDate.getYear())
                .withMonth(readingDate.getMonthValue())
                .withDayOfMonth(1);
        LocalDate billEnd = billStart.withDayOfMonth(billStart.lengthOfMonth());
        LocalDate dueDate = LocalDate.now().plusDays(contractApartment.getPaymentDueDay());

        // Get latest utility readings
        UnitUtilities latestWaterMeter = getLatestUnitUtilitySafe(activeContract.getUnit().getId(), "water", billStart);
        UnitUtilities latestElectricMeter = getLatestUnitUtilitySafe(activeContract.getUnit().getId(), "electric", billStart);

        // Calculate utility amounts
        BigDecimal utilityAmount = calculateUtilityAmountSafe(latestWaterMeter, latestElectricMeter);

        //adhoc invoice for this unit ( unpaid and monthly include )
        List<AdhocInvoice> adhocInvoices = adhocInvoiceRepository.findByUnitAndIncludeInMonthlyAndPaymentStatusAndStatus(
                activeContract.getUnit(),
                true,
                AdhocInvoice.PaymentStatus.unpaid,
                AdhocInvoice.InvoiceStatus.active
        );

        // Calculate adhoc invoice and unit service amounts
        BigDecimal totalAdhocAmount = adhocInvoices.stream()
                .map(AdhocInvoice::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Calculate total unit service amount
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
        Invoice createdMonthlyInvoice = invoiceRepository.save(monthlyInvoice);

        //payment
        Payment payment = new Payment();
        payment.setInvoice(monthlyInvoice);
        payment.setAmount(monthlyInvoice.getTotalAmount());

        ApartmentPayment apartmentPayment = apartmentPaymentRepository.findByApartmentAndIsActive(
                monthlyInvoice.getApartment(),true
        ).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setPaymentMethod(apartmentPayment.getMethodType().toString());
        payment.setPaymentMethodEntity(apartmentPayment);
        paymentRepository.save(payment);

        //add foreign key to adhoc invoice
        List<AdhocInvoice> linkAdhocInvoices = adhocInvoices.stream().peek(item->{
            item.setMonthlyInvoiceId(createdMonthlyInvoice);
        }).toList();
        adhocInvoiceRepository.saveAll(linkAdhocInvoices);

        log.info("Monthly invoice created for unit {} for month {}-{}", unitId, readingDate, dueDate);
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
    private MonthlyInvoiceDetailResponseDto toMonthlyInvoiceDetailDto(Invoice invoice) {
        //find current Apartment
        Apartment apartment = invoice.getApartment();
        //find active payment apartment
        ApartmentPayment currentPayment = apartmentPaymentRepository
                .findByApartmentAndIsActive(apartment, true)
                .orElse(null);

        Payment payment = paymentRepository.findByInvoice(invoice).orElse(null);
        //unit
        Unit currentUnit = invoice.getUnit();

        //find extra service for unit
        List<UnitServiceEntity> unitServices = unitServiceRepository.findAllByUnitId(currentUnit.getId());
        List<UnitServiceList> unitServiceListResult = unitServices.stream().map(unitServiceMapper::toUnitServiceList).toList();

        //find adhoc invoice
        List<AdhocInvoice> adhocInvoices = adhocInvoiceRepository.findByMonthlyInvoiceId(invoice);
        List<UnitAdhocInvoice> unitAdhocInvoices =  adhocInvoices.stream().map(adhocInvoiceMapper::toUnitAdhocInvoice).toList();

        // === find water utility ===
        Specification<UnitUtilities> waterSpec = UnitUtilitySpecification.hasUtilityName("water")
                .and(UnitUtilitySpecification.hasUnitId(currentUnit.getId()))
                .and(UnitUtilitySpecification.hasUsageMonth(invoice.getGenMonth()));
        UnitUtilities waterUtility = unitUtilityRepository.findOne(waterSpec).orElse(null);

        // === find electric utility ===
        Specification<UnitUtilities> electricSpec = UnitUtilitySpecification.hasUtilityName("electric")
                .and(UnitUtilitySpecification.hasUnitId(currentUnit.getId()))
                .and(UnitUtilitySpecification.hasUsageMonth(invoice.getGenMonth()));
        UnitUtilities electricUtility = unitUtilityRepository.findOne(electricSpec).orElse(null);

        // === safe values (if null, fallback to BigDecimal.ZERO or null) ===
        BigDecimal waterAmount = (waterUtility != null && waterUtility.getCalculatedCost() != null)
                ? waterUtility.getCalculatedCost()
                : BigDecimal.ZERO;

        BigDecimal electricAmount = (electricUtility != null && electricUtility.getCalculatedCost() != null)
                ? electricUtility.getCalculatedCost()
                : BigDecimal.ZERO;

        // === get active contract ===
        Contract activeContract = contractRepository.findById(invoice.getContract().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found on this invoice."));

        // === build response ===
        return MonthlyInvoiceDetailResponseDto.builder()
                //apartment Payment
                .apartmentPaymentMethodType(currentPayment != null ? currentPayment.getMethodType(): null)
                .bankName( currentPayment != null ? currentPayment.getBankName() : null)
                .bankAccountNumber(currentPayment != null ? currentPayment.getBankAccountNumber():null)
                .accountHolderName(currentPayment != null ? currentPayment.getAccountHolderName() : null)
                .promptpayNumber(currentPayment != null ? currentPayment.getPromptpayNumber():null)

                //payment
                .paymentId(payment != null ? payment.getId() : null)
                //serviceList
                .serviceList(unitServiceListResult)
                //adhoc invoice
                .unitAdhocInvoices(unitAdhocInvoices)

                //invoice
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .unitName(invoice.getUnit().getUnitName())
                .buildingName(invoice.getUnit().getFloor().getBuilding().getName())
                .tenantName(invoice.getTenant().getFullName())
                .totalAmount(invoice.getTotalAmount())
                .paymentStatus(invoice.getPaymentStatus())
                .tenantPhone(invoice.getTenant().getPhoneNumber())
                .tenantEmail(invoice.getTenant().getEmail())
                .rentAmount(invoice.getRentAmount())
                .contractRentAmount(activeContract.getRentalPrice())
                .floorName(invoice.getUnit().getFloor().getFloorName())
                .contractNumber(activeContract.getContractNumber())
                .dueDate(invoice.getDueDate())
                .rentalType(activeContract.getRentalType())
                .createdAt(invoice.getCreatedAt())
                // === Water section ===
                .waterAmount(waterAmount)
                .waterMeterStart(waterUtility != null ? waterUtility.getMeterStart() : null)
                .waterMeterEnd(waterUtility != null ? waterUtility.getMeterEnd() : null)
                .totalWaterUsageUnit(waterUtility != null ? waterUtility.getUsageAmount() : null)
                .waterPricePerUnit(waterUtility != null && waterUtility.getUtility() != null ? waterUtility.getUtility().getUnitPrice() : null)
                .waterPriceRateType(waterUtility != null && waterUtility.getUtility() != null ? waterUtility.getUtility().getUtilityType() : null)
                .waterTotalCost(waterUtility != null ? waterUtility.getCalculatedCost() : BigDecimal.ZERO)
                .waterFixedPrice(waterUtility != null && waterUtility.getUtility() != null ? waterUtility.getUtility().getFixedPrice() : null)

                // === Electric section ===
                .electricAmount(electricAmount)
                .electricMeterStart(electricUtility != null ? electricUtility.getMeterStart() : null)
                .electricMeterEnd(electricUtility != null ? electricUtility.getMeterEnd() : null)
                .totalElectricUsageUnit(electricUtility != null ? electricUtility.getUsageAmount() : null)
                .electricPricePerUnit(electricUtility != null && electricUtility.getUtility() != null ? electricUtility.getUtility().getUnitPrice() : null)
                .electricPriceRateType(electricUtility != null && electricUtility.getUtility() != null ? electricUtility.getUtility().getUtilityType() : null)
                .electricTotalCost(electricUtility != null ? electricUtility.getCalculatedCost() : BigDecimal.ZERO)
                .electricFixedPrice(electricUtility != null && electricUtility.getUtility() != null ? electricUtility.getUtility().getFixedPrice() : null)

                // === Other info ===
                .billStart(invoice.getBillStart())
                .billEnd(invoice.getBillEnd())
                .build();
    }

    private MonthlyInvoiceResponseDto toMonthlyInvoiceResponseDto(Invoice invoice) {
        // water
        Specification<UnitUtilities> waterUnitUtilitySpecification = UnitUtilitySpecification.hasUtilityName("water")
                .and(UnitUtilitySpecification.hasUnitId(invoice.getUnit().getId()))
                .and(UnitUtilitySpecification.hasUsageMonth(invoice.getGenMonth()));

        UnitUtilities waterUtility = unitUtilityRepository.findOne(waterUnitUtilitySpecification).orElse(null);

        // electric
        Specification<UnitUtilities> electricUnitUtilitySpecification = UnitUtilitySpecification.hasUtilityName("electric")
                .and(UnitUtilitySpecification.hasUnitId(invoice.getUnit().getId()))
                .and(UnitUtilitySpecification.hasUsageMonth(invoice.getGenMonth()));

        UnitUtilities electricUtility = unitUtilityRepository.findOne(electricUnitUtilitySpecification).orElse(null);

        // safely handle nulls using ternary operators (or Optional)
        BigDecimal waterAmount = (waterUtility != null && waterUtility.getCalculatedCost() != null)
                ? waterUtility.getCalculatedCost()
                : BigDecimal.ZERO;

        BigDecimal electricAmount = (electricUtility != null && electricUtility.getCalculatedCost() != null)
                ? electricUtility.getCalculatedCost()
                : BigDecimal.ZERO;

        return MonthlyInvoiceResponseDto.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .tenantName(invoice.getTenant().getFullName())
                .tenantPhone(invoice.getTenant().getPhoneNumber())
                .buildingName(invoice.getContract().getUnit().getFloor().getBuilding().getName())
                .unitName(invoice.getContract().getUnit().getUnitName())
                .paymentStatus(invoice.getPaymentStatus())
                .rentAmount(invoice.getRentAmount())
                .waterAmount(waterAmount)
                .electricAmount(electricAmount)
                .totalAmount(invoice.getTotalAmount())
                .build();
    }


}
