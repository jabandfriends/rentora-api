package com.rentora.api.mapper;

import com.rentora.api.model.dto.Invoice.Request.AdhocUpdateRequestResponseDto;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceSummaryDTO;
import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitAdhocInvoice;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class AdhocInvoiceMapper {
    private final S3FileService s3FileService;

    public AdhocUpdateRequestResponseDto toAdhocUpdateRequestResponseDto(AdhocInvoice adhocInvoice, URL presignedUrl) {
        return AdhocUpdateRequestResponseDto.builder()
                .invoiceId(adhocInvoice.getId())
                .presignedUrl(presignedUrl)
                .build();

    }
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

        if(adhocInvoice.getReceiptUrls() != null && !adhocInvoice.getReceiptUrls().isEmpty()) {
            URL receiptImg = s3FileService.generatePresignedUrlForGet(adhocInvoice.getReceiptUrls());
            detail.setReceiptUrls(receiptImg);
        }
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
