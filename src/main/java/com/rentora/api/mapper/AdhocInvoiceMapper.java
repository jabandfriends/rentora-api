package com.rentora.api.mapper;

import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceSummaryDTO;
import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitAdhocInvoice;
import com.rentora.api.model.entity.AdhocInvoice;
import org.springframework.stereotype.Component;

@Component
public class AdhocInvoiceMapper {
    public UnitAdhocInvoice toUnitAdhocInvoice(AdhocInvoice adhocInvoice) {
        return UnitAdhocInvoice.builder()
                .adhocId(adhocInvoice.getId())
                .adhocTitle(adhocInvoice.getTitle())
                .adhocNumber(adhocInvoice.getAdhocNumber())
                .amount(adhocInvoice.getFinalAmount())
                .build();
    }
    public AdhocInvoiceSummaryDTO toAdhocInvoiceSummaryDTO(AdhocInvoice adhocInvoice) {
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
    public AdhocInvoiceDetailDTO toAdhocInvoiceDetailDTO(AdhocInvoice adhocInvoice) {
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
}
