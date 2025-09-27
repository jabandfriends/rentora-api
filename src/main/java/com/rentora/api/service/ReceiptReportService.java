package com.rentora.api.service;

import com.rentora.api.model.dto.Report.Response.ReceiptReportDetailDTO;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.repository.ReceiptReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReceiptReportService {

    private final ReceiptReportRepository adhocInvoiceRepository;

    public Page<ReceiptReportDetailDTO> getAdhocInvoices(UUID apartmentId, AdhocInvoice.PaymentStatus status, Pageable pageable) {
        Page<AdhocInvoice> invoices;

        if (status != null) {
            invoices = adhocInvoiceRepository.findByApartmentIdAndPaymentStatus(apartmentId, status, pageable);
        } else {
            invoices = adhocInvoiceRepository.findByApartmentId(apartmentId, pageable);
        }

        return invoices.map(this::toDto);
    }

    private ReceiptReportDetailDTO toDto(AdhocInvoice entity) {
        ReceiptReportDetailDTO dto = new ReceiptReportDetailDTO();
        dto.setId(entity.getId().toString());
        dto.setAdhocNumber(entity.getAdhocNumber());
        dto.setApartmentId(entity.getApartmentId().toString());
        dto.setUnitId(entity.getUnit() != null ? entity.getUnit().getId().toString() : null);
        dto.setTenantUserId(entity.getTenantUserId() != null ? entity.getTenantUserId().toString() : null);
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setFinalAmount(entity.getFinalAmount());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setInvoiceDate(entity.getInvoiceDate() != null ? entity.getInvoiceDate().toString() : null);
        dto.setDueDate(entity.getDueDate() != null ? entity.getDueDate().toString() : null);
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return dto;
    }
}


