package com.rentora.api.service;

import java.util.UUID;

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

    private final InvoiceRepository {}
    public Page<InvoiceSummaryDTO> search(String invoiceNumber,
                                Invoice.PaymentStatus status,
                                Pageable pageable) {
        Specification<Invoice> specification = Specification
                .anyOf(InvoiceSpecification.invoiceNumberContains(invoiceNumber),InvoiceSpecification.status(status));

        Page<Invoice> InvoiceDetail = invoiceRepository.findAll(specification, pageable);

        return InvoiceDetail.map(this::toInvoicesSummaryDTO);
    }
        
    // public Page<InvoiceDTO> getInvoices(UUID userId, String search, Pageable pageable) {
    //     Page<Invoice> invoices;

    //     if (search != null && !search.trim().isEmpty()) {

    //         invoices = Specification.where(InvoiceSpecification.invoiceNumberContains(search));
    //     } else {

    //         invoices = invoiceRepository.getAllInvoices(userId, pageable);
    //     }
    //     return invoices.map(this::toInvoicesDTO);
    // }

//    public InvoiceDetailDTO getInvoicesById(UUID invoiceId, UUID userId, String search) {
//        Invoice invoice = invoiceRepository.findById(invoiceId)
//                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
//
//        return toInvoicesDetailDTO(invoice);
//    }

    private InvoiceSummaryDTO toInvoicesSummaryDTO(Invoice invoice) {
        InvoiceSummaryDTO dto = new InvoiceSummaryDTO();
        dto.setId(invoice.getId().toString());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());

        if (invoice.getTenant() != null) {
            dto.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
        }

        dto.setRoom(invoice.getUnit().toString());
        dto.setAmount(invoice.getTotalAmount());
        // dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getPaymentStatus());

        return dto;
    }

        private InvoiceDetailDTO toInvoicesDetailDTO(Invoice invoice) {
        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setId(invoice.getId().toString());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());

        if (invoice.getTenant() != null) {
            dto.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
        }

        dto.setRoom(invoice.getUnit().toString());
        dto.setAmount(invoice.getTotalAmount());
        // dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getPaymentStatus());

        return dto;
    }

}
