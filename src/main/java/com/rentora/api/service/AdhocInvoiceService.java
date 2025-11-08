package com.rentora.api.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rentora.api.model.dto.Invoice.Metadata.AdhocInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Metadata.OverdueInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Request.CreateAdhocInvoiceRequest;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceSummaryDTO;
import com.rentora.api.model.dto.Invoice.Response.ExecuteAdhocInvoiceResponse;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.specifications.AdhocInvoiceSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Page<AdhocInvoiceSummaryDTO> searchAdhocInvoiceByInvoiceNumber(String invoiceNumber,
                                          AdhocInvoice.PaymentStatus status,
                                          Pageable pageable , UUID apartmentId) {
        Specification<AdhocInvoice> specification = Specification
                .anyOf(AdhocInvoiceSpecification.hasInvoiceNumberForAdhoc(invoiceNumber), AdhocInvoiceSpecification.hasStatusForAdhoc(status)).and(AdhocInvoiceSpecification.hasApartmentIdForAdhoc(apartmentId));
        if (status != null) {
            specification = specification.and(AdhocInvoiceSpecification.hasStatusForAdhoc(status));
        }

        Page<AdhocInvoice> allAdhocInvoices = invoiceRepository.findAll(specification,pageable);

        return allAdhocInvoices.map(AdhocInvoiceService::toAdhocInvoiceSummaryDTO);
    }

    public Page<AdhocInvoiceSummaryDTO> searchAdhocInvoiceOverdue(String invoiceNumber,
                                                      Pageable pageable, UUID apartmentId) {

        Specification<AdhocInvoice> specification = Specification.allOf(AdhocInvoiceSpecification.hasInvoiceNumberForAdhoc(invoiceNumber), AdhocInvoiceSpecification.hasOverdueStatusForAdhoc()).and(AdhocInvoiceSpecification.hasApartmentIdForAdhoc(apartmentId));
        Page<AdhocInvoice> OverdueInvoice = invoiceRepository.findAll(specification, pageable);

        return OverdueInvoice.map(AdhocInvoiceService::toAdhocInvoiceSummaryDTO);
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

        AdhocInvoiceDetailDTO dto = toAdhocInvoiceDetailDTO(adhocInvoice);

        return dto;
    }


    //for get overall of overdue invoice
    public OverdueInvoiceOverallDTO getOverdueAdhocInvoiceOverall(List<AdhocInvoiceSummaryDTO> listOverDue) {
        OverdueInvoiceOverallDTO overdue = new OverdueInvoiceOverallDTO();
        overdue.setOverdueInvoice(listOverDue.size());

        return overdue;
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

    private static AdhocInvoiceSummaryDTO toAdhocInvoiceSummaryDTO(AdhocInvoice adhocInvoice) {
        AdhocInvoiceSummaryDTO summary = new AdhocInvoiceSummaryDTO();
        summary.setId(adhocInvoice.getId());
        summary.setInvoiceNumber(adhocInvoice.getAdhocNumber());
        summary.setTitle(adhocInvoice.getTitle());
        summary.setDescription(adhocInvoice.getDescription());
        if (adhocInvoice.getTenantUserId() != null) {
            summary.setTenant(adhocInvoice.getTenantUserId().getFirstName() + " " + adhocInvoice.getTenantUserId().getLastName());
        }
        summary.setRoom(adhocInvoice.getUnit().getUnitName());
        summary.setAmount(adhocInvoice.getFinalAmount());
        summary.setIssueDate(adhocInvoice.getInvoiceDate());
        summary.setDueDate(adhocInvoice.getDueDate());
        summary.setStatus(adhocInvoice.getPaymentStatus());

        return summary;
    }

    private static AdhocInvoiceDetailDTO toAdhocInvoiceDetailDTO(AdhocInvoice adhocInvoice) {
        AdhocInvoiceDetailDTO detail = new AdhocInvoiceDetailDTO();
        detail.setAdhocInvoiceId(adhocInvoice.getId());
        detail.setCategory(adhocInvoice.getCategory());
        detail.setAdhocNumber(adhocInvoice.getAdhocNumber());
        detail.setTitle(adhocInvoice.getTitle());
        detail.setDescription(adhocInvoice.getDescription());
        detail.setPaymentStatus(adhocInvoice.getPaymentStatus());
        detail.setStatus(adhocInvoice.getStatus());
        detail.setPriority(adhocInvoice.getPriority());
        detail.setFinalAmount(adhocInvoice.getFinalAmount());
        detail.setPaidAmount(adhocInvoice.getPaidAmount());
        detail.setInvoiceDate(adhocInvoice.getInvoiceDate());
        detail.setDueDate(adhocInvoice.getDueDate());

        if (adhocInvoice.getApartment() != null) {
            detail.setApartment(adhocInvoice.getApartment().getName());
        }

        if (adhocInvoice.getUnit() != null) {
            detail.setUnit(adhocInvoice.getUnit().getUnitName());
        }

        if (adhocInvoice.getTenantUserId() != null) {
            detail.setTenantUser(adhocInvoice.getTenantUserId().getFirstName() + " " + adhocInvoice.getTenantUserId().getLastName());
            detail.setEmail(adhocInvoice.getTenantUserId().getEmail());
        }

        detail.setReceiptUrls(adhocInvoice.getReceiptUrls());
        detail.setImages(adhocInvoice.getImages());
        detail.setNotes(adhocInvoice.getNotes());
        if(adhocInvoice.getCreatedByUserId() != null) {
            detail.setCreatedByUserId(adhocInvoice.getCreatedByUserId().getId());
        }

        detail.setCreatedAt(adhocInvoice.getCreatedAt());
        detail.setUpdatedAt(adhocInvoice.getUpdatedAt());
        return detail;
    }

    private static InvoiceDetailDTO toInvoicesDetailDTO(Invoice invoice) {
        InvoiceDetailDTO detail = new InvoiceDetailDTO();
        detail.setId(invoice.getId());
        detail.setInvoiceNumber(invoice.getInvoiceNumber());
        detail.setContract(invoice.getContract().getContractNumber());
        detail.setStatus(invoice.getPaymentStatus());

        detail.setRentalAmount(invoice.getRentAmount());
        detail.setUtilAmount(invoice.getUtilAmount());
        detail.setServiceAmount(invoice.getServiceAmount());
        detail.setFeesAmount(invoice.getFeesAmount());
        detail.setDiscountAmount(invoice.getDiscountAmount());
        detail.setTaxAmount(invoice.getTaxAmount());
        detail.setTotalAmount(invoice.getTotalAmount());
        detail.setBillStart(invoice.getBillStart());
        detail.setDueDate(invoice.getDueDate());

        if (invoice.getApartment() != null) {
            detail.setApartment(invoice.getApartment().getName());
            detail.setUnit(invoice.getUnit().getFloor().getFloorName());
            detail.setRoom(invoice.getUnit().getUnitName());
        }

        if (invoice.getTenant() != null) {
            detail.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
            detail.setEmail(invoice.getTenant().getEmail());
        }
        detail.setPdf(invoice.getPdf());
        detail.setNotes(invoice.getNotes());
        detail.setCreatedAt(invoice.getCreatedAt());
        detail.setUpdatedAt(invoice.getUpdatedAt());

        return detail;
    }

}
