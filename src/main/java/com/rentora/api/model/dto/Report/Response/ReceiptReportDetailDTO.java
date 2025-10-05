package com.rentora.api.model.dto.Report.Response;

import com.rentora.api.model.entity.AdhocInvoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ReceiptReportDetailDTO {
    private String id;
    private String adhocNumber;
    private BigDecimal amount;

    // Relation
    private String apartmentId;
    private String unitId;
    private String tenantUserId;

    // Invoice Info
    private String title;
    private String description;
    private String category;

    private BigDecimal finalAmount;
    private BigDecimal paidAmount;

    private String invoiceDate;
    private String dueDate;

    private Boolean includeInMonthly;
    private String targetMonthlyInvoiceMonth;
    private String monthlyInvoiceId;

    private String includedAt;
    private AdhocInvoice.PaymentStatus paymentStatus;
    private String paidAt;

    // Status
    private AdhocInvoice.InvoiceStatus status;
    private AdhocInvoice.InvoicePriority priority;

    // Files
    private String receiptUrls; // JSON string
    private String images;      // JSON string

    private String notes;

    // Audit
    private String createdByUserId;
    private String approvedByUserId;
    private String approvedAt;

    private String createdAt;
    private String updatedAt;
}
