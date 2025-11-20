package com.rentora.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rentora.api.mapper.AdhocInvoiceMapper;
import com.rentora.api.model.dto.Invoice.Metadata.AdhocInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Metadata.OverdueInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Request.AdhocInvoiceUpdateRequestDto;
import com.rentora.api.model.dto.Invoice.Request.AdhocUpdateRequestResponseDto;
import com.rentora.api.model.dto.Invoice.Request.CreateAdhocInvoiceRequest;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceSummaryDTO;
import com.rentora.api.model.dto.Invoice.Response.ExecuteAdhocInvoiceResponse;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.*;
import com.rentora.api.specifications.ContractSpecification;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.specifications.AdhocInvoiceSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.rentora.api.model.dto.Invoice.Response.InvoiceDetailDTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdhocInvoiceService {

    private final AdhocInvoiceRepository invoiceRepository;
    private final UnitRepository unitRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    private final AdhocInvoiceMapper adhocInvoiceMapper;

    private final S3FileService s3FileService;

    public Page<AdhocInvoiceSummaryDTO> searchAdhocInvoiceByInvoiceNumber(String invoiceNumber,
                                          AdhocInvoice.PaymentStatus status,
                                          Pageable pageable , UUID apartmentId) {
        Specification<AdhocInvoice> specification = Specification
                .anyOf(AdhocInvoiceSpecification.hasInvoiceNumberForAdhoc(invoiceNumber), AdhocInvoiceSpecification.hasStatusForAdhoc(status)).and(AdhocInvoiceSpecification.hasApartmentIdForAdhoc(apartmentId));
        if (status != null) {
            specification = specification.and(AdhocInvoiceSpecification.hasStatusForAdhoc(status));
        }

        Page<AdhocInvoice> allAdhocInvoices = invoiceRepository.findAll(specification,pageable);

        return allAdhocInvoices.map(adhocInvoiceMapper::toAdhocInvoiceSummaryDTO);
    }


    public Page<AdhocInvoiceSummaryDTO> searchAdhocInvoiceOverdue(String invoiceNumber,
                                                      Pageable pageable, UUID apartmentId) {

        Specification<AdhocInvoice> specification = Specification.allOf(AdhocInvoiceSpecification.hasInvoiceNumberForAdhoc(invoiceNumber), AdhocInvoiceSpecification.hasOverdueStatusForAdhoc()).and(AdhocInvoiceSpecification.hasApartmentIdForAdhoc(apartmentId));
        Page<AdhocInvoice> OverdueInvoice = invoiceRepository.findAll(specification, pageable);

        return OverdueInvoice.map(adhocInvoiceMapper::toAdhocInvoiceSummaryDTO);
    }

    public AdhocInvoiceOverallDTO getAdhocInvoiceOverall(List<AdhocInvoiceSummaryDTO> listOverAll) {
        AdhocInvoiceOverallDTO overall = new AdhocInvoiceOverallDTO();
        overall.setTotalInvoice(listOverAll.size());

        Map<AdhocInvoice.PaymentStatus, Long> statusCount = listOverAll.stream().collect(Collectors.groupingBy(AdhocInvoiceSummaryDTO::getStatus, Collectors.counting()));

        overall.setPaidInvoice(statusCount.getOrDefault(AdhocInvoice.PaymentStatus.paid, 0L));
        overall.setUnpaidInvoice(statusCount.getOrDefault(AdhocInvoice.PaymentStatus.unpaid, 0L));
        overall.setOverdueInvoice(statusCount.getOrDefault(AdhocInvoice.PaymentStatus.overdue,0L));

        return overall;

    }

    public AdhocInvoiceDetailDTO getAdhocInvoicesById(UUID adhocInvoiceId, UUID apartmentId) {
        Specification<AdhocInvoice>  specification = AdhocInvoiceSpecification.hasApartmentIdForAdhoc(apartmentId).and(AdhocInvoiceSpecification.hasAdhocId(adhocInvoiceId));
        AdhocInvoice adhocInvoice = invoiceRepository.findOne(specification)
                .orElseThrow(() -> new ResourceNotFoundException("AdhocInvoice not found or access denied"));

        return adhocInvoiceMapper.toAdhocInvoiceDetailDTO(adhocInvoice);
    }

    public Page<AdhocInvoiceSummaryDTO> getAdhocInvoicesByTenant(
            UUID tenantUserId,
            UUID apartmentId,
            AdhocInvoice.PaymentStatus paymentStatus,
            AdhocInvoice.AdhocInvoiceCategory category,
            Pageable pageable) {

        userRepository.findById(tenantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant user not found with ID: " + tenantUserId));

        Specification<Contract> contractSpec = ContractSpecification.hasApartmentId(apartmentId)
                .and(ContractSpecification.hasTenantId(tenantUserId))
                .and(ContractSpecification.hasStatus(Contract.ContractStatus.active));

        Contract activeContract = contractRepository.findOne(contractSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Active Contract not found for tenant: " + tenantUserId + " in apartment: " + apartmentId));

        UUID unitId = activeContract.getUnit().getId();

        Specification<AdhocInvoice> invoiceSpec = AdhocInvoiceSpecification.hasUnitIdForAdhoc(unitId);

        Page<AdhocInvoice> adhocInvoices = invoiceRepository.findAll(invoiceSpec, pageable);

        return adhocInvoices.map(adhocInvoiceMapper::toAdhocInvoiceSummaryDTO);
    }


    //for get overall of overdue invoice
    public OverdueInvoiceOverallDTO getOverdueAdhocInvoiceOverall(List<AdhocInvoiceSummaryDTO> listOverDue) {
        OverdueInvoiceOverallDTO overdue = new OverdueInvoiceOverallDTO();
        overdue.setOverdueInvoice(listOverDue.size());

        return overdue;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void applyLateFeeForNotIncludeMonthlyAdhocInvoice() {
        Specification<AdhocInvoice> specification = AdhocInvoiceSpecification.hasStatusForAdhoc(AdhocInvoice.PaymentStatus.unpaid);
        List<AdhocInvoice> adhocInvoices = invoiceRepository.findAll(specification);

        for (AdhocInvoice adhocInvoice : adhocInvoices) {
            Apartment apartment = adhocInvoice.getApartment();
            //get setting
            Integer gracePeriodDays = apartment.getGracePeriodDays();
            Apartment.LateFeeType lateFeeType = apartment.getLateFeeType();
            BigDecimal lateFeeAmount = apartment.getLateFee();
            //check late
            LocalDate overDueDay = adhocInvoice.getDueDate().plusDays(gracePeriodDays);
            long overDueDays = ChronoUnit.DAYS.between(overDueDay, LocalDate.now());
            if(overDueDays > 0){
                adhocInvoice.setPaymentStatus(AdhocInvoice.PaymentStatus.overdue);
                //let monthly do it
                if(adhocInvoice.getIncludeInMonthly()) continue;
                BigDecimal currentAmount = adhocInvoice.getFinalAmount();
                if(lateFeeType.equals(Apartment.LateFeeType.fixed)){
                    currentAmount = currentAmount.add(lateFeeAmount);
                    adhocInvoice.setFinalAmount(currentAmount);

                }else if(lateFeeType.equals(Apartment.LateFeeType.percentage)){
                    BigDecimal addedFee = currentAmount
                            .multiply(lateFeeAmount)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    currentAmount = currentAmount.add(addedFee);
                    adhocInvoice.setFinalAmount(currentAmount);

                }
            }
            invoiceRepository.save(adhocInvoice);
        }
    }


    public ExecuteAdhocInvoiceResponse createAdhocInvoice(UUID createdByUserId, UUID apartmentId, CreateAdhocInvoiceRequest request) {

        //create by
        User user = userRepository.findById(createdByUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(()-> new ResourceNotFoundException("Unit not found with ID: " + request.getUnitId()));

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(()-> new ResourceNotFoundException("Unit not found with ID: " + apartmentId));

        List<Contract> contracts = unit.getContracts();

        Optional<Contract> activeContract = contracts.stream()
                .filter(contract -> contract.getStatus().equals(Contract.ContractStatus.active)).findFirst();

        AdhocInvoice adhocInvoice = new AdhocInvoice();

        activeContract.ifPresent(contract -> adhocInvoice.setTenantUserId(contract.getTenant()));

        adhocInvoice.setUnit(unit);
        adhocInvoice.setApartment(apartment);
        adhocInvoice.setTitle(request.getTitle());
        adhocInvoice.setDescription(request.getDescription());
        adhocInvoice.setInvoiceDate(request.getInvoiceDate());
        adhocInvoice.setDueDate(request.getDueDate());
        adhocInvoice.setCategory(request.getCategory());
        adhocInvoice.setFinalAmount(request.getFinalAmount());
        adhocInvoice.setPaymentStatus(request.getPaymentStatus());
        adhocInvoice.setNotes(request.getNotes());
        adhocInvoice.setIncludeInMonthly(request.getIncludeInMonthly());
        adhocInvoice.setPriority(request.getPriority());
        adhocInvoice.setStatus(request.getStatus());
        adhocInvoice.setCreatedByUserId(user);

        AdhocInvoice savedAdhocInvoice = invoiceRepository.save(adhocInvoice);

        return new ExecuteAdhocInvoiceResponse(savedAdhocInvoice.getId());
    }


    public AdhocUpdateRequestResponseDto updateAdhocInvoice(AdhocInvoiceUpdateRequestDto requestDto){
        AdhocInvoice invoice = invoiceRepository.findById(requestDto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + requestDto.getInvoiceId()));

        if(requestDto.getTitle() != null && !requestDto.getTitle().isEmpty()) invoice.setTitle(requestDto.getTitle());
        if(requestDto.getDescription() != null && !requestDto.getDescription().isEmpty()) invoice.setDescription(requestDto.getDescription());
        if(requestDto.getCategory() != null) invoice.setCategory(requestDto.getCategory());
        if(requestDto.getAmount() != null) invoice.setFinalAmount(requestDto.getAmount());
        if(requestDto.getDueDate() != null) invoice.setDueDate(requestDto.getDueDate());

        //payment Status
        if(requestDto.getPaymentStatus() != null) invoice.setPaymentStatus(requestDto.getPaymentStatus());
        if(requestDto.getPaymentStatus() != null && requestDto.getPaymentStatus().equals(AdhocInvoice.PaymentStatus.paid)){
            invoice.setPaidAt(OffsetDateTime.now());
        }else{
            invoice.setPaidAt(null);
        }

        if(requestDto.getInvoiceStatus() != null) invoice.setStatus(requestDto.getInvoiceStatus());
        if(requestDto.getInvoicePriority() != null) invoice.setPriority(requestDto.getInvoicePriority());

        URL presignedUrl = null;
        if(requestDto.getFileName() != null && !requestDto.getFileName().isEmpty()){
            //delete previous
            if(invoice.getReceiptUrls() != null && !invoice.getReceiptUrls().isEmpty()) {
                s3FileService.deleteFile(invoice.getReceiptUrls());
            }
            String logoImgKey = "apartments/adhoc_invoice/"+invoice.getId()+ LocalDateTime.now()+requestDto.getFileName();
            try {
                presignedUrl = s3FileService.generatePresignedUrlForPut(logoImgKey);
                invoice.setReceiptUrls(logoImgKey);
            } catch (Exception e) {
                log.warn("Failed to generate presigned PUT URL for apartment logo: {}", e.getMessage());
            }
        }

        AdhocInvoice savedInvoice = invoiceRepository.save(invoice);
        return adhocInvoiceMapper.toAdhocUpdateRequestResponseDto(savedInvoice,presignedUrl);

    }




}
