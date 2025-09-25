package com.rentora.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.rentora.api.model.dto.Invoice.Response.InvoiceOverallDTO;
import com.rentora.api.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
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

//    public InvoiceOverallDTO getOverallInvoice(UUID userid,Invoice.PaymentStatus status){
//
//        List<Invoice> invoiceSummaries = invoiceRepository.findAll();
//        List<InvoiceOverallDTO> invoiceOverallDTO = invoiceSummaries.map(InvoiceService::toInvoicesSummaryDTO).getContent();
//    }

    public Page<InvoiceSummaryDTO> search(String invoiceNumber,
                                Invoice.PaymentStatus status,
                                Pageable pageable) {
        Specification<Invoice> specification = Specification
                .anyOf(InvoiceSpecification.hasInvoiceNumber(invoiceNumber),InvoiceSpecification.hasStatus(status));

        Page<Invoice> allInvoice = invoiceRepository.findAll(specification, pageable);

        return allInvoice.map(InvoiceService::toInvoicesSummaryDTO);
    }

//    public InvoiceOverallDTO getInvoiceSummary(String invoiceNumber, Invoice.PaymentStatus status, Pageable pageable) {
//        Specification<Invoice> specification = Specification.anyOf(InvoiceSpecification.hasInvoiceNumber(invoiceNumber),InvoiceSpecification.hasStatus(status));
//
//
//        Page<Invoice> allInvoices = invoiceRepository.findAll(specification,pageable);
//
//        List<InvoiceSummaryDTO> invoiceSummaries = allInvoices.map(InvoiceService::toInvoicesSummaryDTO).getContent();
//
//        long total = allInvoices.getTotalElements();
//        long paid =  allInvoices.getTotalPages();
//        long unpaid = total - paid;
//
//        InvoiceOverallDTO overall = new InvoiceOverallDTO();
//        overall.setOverallDTO(invoiceSummaries);
//        overall.setTotalInvoice(total);
//
//        return overall;
//    }

    public InvoiceDetailDTO getInvoicesById(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found or access denied"));

        InvoiceDetailDTO dto = toInvoicesDetailDTO(invoice);

        return dto;
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
