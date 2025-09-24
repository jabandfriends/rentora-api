package com.rentora.api.service;

import java.util.UUID;

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

    public Page<InvoiceSummaryDTO> search(String invoiceNumber,
                                Invoice.PaymentStatus status,
                                Pageable pageable) {
        Specification<Invoice> specification = Specification
                .anyOf(InvoiceSpecification.hasInvoiceNumber(invoiceNumber),InvoiceSpecification.hasStatus(status));

        Page<Invoice> InvoiceSummary = invoiceRepository.findAll(specification, pageable);

        return InvoiceSummary.map(this::toInvoicesSummaryDTO);
    }

    public InvoiceDetailDTO getInvoicesById(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found or access denied"));

        InvoiceDetailDTO dto = toInvoicesDetailDTO(invoice);

        return dto;
    }

    private InvoiceSummaryDTO toInvoicesSummaryDTO(Invoice invoice) {
        InvoiceSummaryDTO dto = new InvoiceSummaryDTO();
        dto.setId(invoice.getId().toString());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());

        if (invoice.getTenant() != null) {
            dto.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
        }

        dto.setRoom(invoice.getUnit().toString());
        dto.setAmount(invoice.getTotalAmount());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getPaymentStatus());

        return dto;
    }

        private InvoiceDetailDTO toInvoicesDetailDTO(Invoice invoice) {
        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setContract(invoice.getContract().toString());
        dto.setStatus(invoice.getPaymentStatus());

        dto.setRentalAmount(invoice.getRentAmount());
        dto.setUtilAmount(invoice.getUtilAmount());
        dto.setServiceAmount(invoice.getServiceAmount());
        dto.setFeesAmount(invoice.getFeesAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setBillStart(invoice.getBillStart());
        dto.setDueDate(invoice.getDueDate());

        if (invoice.getApartment() != null) {
            dto.setApartment(invoice.getApartment().toString());
            dto.setUnit(invoice.getUnit().toString());
            dto.setRoom(invoice.getUnit().toString());
        }

        if (invoice.getTenant() != null) {
            dto.setTenant(invoice.getTenant().getFirstName() + " " + invoice.getTenant().getLastName());
            dto.setEmail(invoice.getTenant().getEmail());
        }
        dto.setPdf(invoice.getPdf());
        dto.setNotes(invoice.getNotes());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        return dto;
    }

}
