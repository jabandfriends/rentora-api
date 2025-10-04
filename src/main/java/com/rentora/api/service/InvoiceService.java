package com.rentora.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rentora.api.model.dto.Invoice.Metadata.InvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Response.OverdueInvoiceOverallDTO;
import com.rentora.api.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.specifications.InvoiceSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.rentora.api.model.dto.Invoice.Response.InvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.InvoiceSummaryDTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

//method for monthly invoice

    //for searching in invoice table
    public Page<InvoiceSummaryDTO> search(String invoiceNumber,
                                Invoice.PaymentStatus status,
                                Pageable pageable , UUID apartmentId) {
        Specification<Invoice> specification = Specification
                .anyOf(InvoiceSpecification.hasInvoiceNumber(invoiceNumber),InvoiceSpecification.hasStatus(status)).and(InvoiceSpecification.hasApartmentId(apartmentId));
        if (status != null) {
            specification = specification.and(InvoiceSpecification.hasStatus(status));
        }

        Page<Invoice> allInvoice = invoiceRepository.findAll(specification,pageable);

        return allInvoice.map(InvoiceService::toInvoicesSummaryDTO);
    }

    //for get overall invoice
    public InvoiceOverallDTO getInvoiceOverall(List<InvoiceSummaryDTO> listOverAll) {
        InvoiceOverallDTO overall = new InvoiceOverallDTO();
        overall.setTotalInvoice(listOverAll.size());

        Map<Invoice.PaymentStatus, Long> statusCount = listOverAll.stream().collect(Collectors.groupingBy(InvoiceSummaryDTO::getStatus, Collectors.counting()));

        overall.setPaidInvoice(statusCount.getOrDefault(Invoice.PaymentStatus.paid, 0L));
        overall.setUnpaidInvoice(statusCount.getOrDefault(Invoice.PaymentStatus.unpaid, 0L));
        overall.setPartiallyPaidInvoice(statusCount.getOrDefault(Invoice.PaymentStatus.partially_paid, 0L));
        overall.setOverdueInvoice(statusCount.getOrDefault(Invoice.PaymentStatus.overdue,0L));
        overall.setCancelledInvoice(statusCount.getOrDefault(Invoice.PaymentStatus.cancelled,0L));

        return overall;

    }

    //for get invoice by using invoice id
    public InvoiceDetailDTO getInvoicesById(UUID invoiceId, UUID userId, UUID apartmentId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found or access denied"));

        InvoiceDetailDTO dto = toInvoicesDetailDTO(invoice);

        return dto;
    }

//method for overdue invoice
    //for searching overdue invoice in table
    public Page<InvoiceSummaryDTO> searchOverdue(String invoiceNumber,
                                                 Pageable pageable, UUID apartmentId) {

        Specification<Invoice> specification = Specification.allOf(InvoiceSpecification.hasOverdueStatus(),InvoiceSpecification.hasApartmentId(apartmentId));
        Page<Invoice> OverdueInvoice = invoiceRepository.findAll(specification, pageable);

        return OverdueInvoice.map(InvoiceService::toInvoicesSummaryDTO);
    }

    //for get overall of overdue invoice
    public OverdueInvoiceOverallDTO getOverdueInvoiceOverall(List<InvoiceSummaryDTO> listOverDue) {
        OverdueInvoiceOverallDTO overdue = new OverdueInvoiceOverallDTO();
        overdue.setOverdueInvoice(listOverDue.size());

        return overdue;
    }

    private static InvoiceSummaryDTO toInvoicesSummaryDTO(Invoice invoice) {
        InvoiceSummaryDTO summary = new InvoiceSummaryDTO();
        summary.setId(invoice.getId());
        summary.setInvoiceNumber(invoice.getInvoiceNumber());


        if (invoice.getTenant() != null) {
            summary.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
        }

        summary.setRoom(invoice.getUnit().getUnitName());
        summary.setAmount(invoice.getTotalAmount());
        summary.setIssueDate(invoice.getBillStart());
        summary.setDueDate(invoice.getDueDate());
        summary.setStatus(invoice.getPaymentStatus());

        return summary;
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
