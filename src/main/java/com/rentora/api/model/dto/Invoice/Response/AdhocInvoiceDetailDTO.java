package com.rentora.api.model.dto.Invoice.Response;

import com.rentora.api.model.entity.AdhocInvoice;
import lombok.Data;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AdhocInvoiceDetailDTO {

    private UUID adhocInvoiceId;

    //overview
    private String adhocNumber;
    private String title;
    private String description;
    private AdhocInvoice.PaymentStatus paymentStatus;
    private AdhocInvoice.InvoiceStatus status;
    private AdhocInvoice.InvoicePriority priority;
    private AdhocInvoice.AdhocInvoiceCategory category;

    //Amount
    private BigDecimal finalAmount;
    private BigDecimal paidAmount;

    //important date
    private LocalDate invoiceDate;
    private LocalDate dueDate;

    //priority detail
    private String apartment;
    private String unit;

    //tenant
    private String tenantUser;
    private String email;

    //document
    private URL receiptUrls;
    private String images;

    //notes
    private String notes;


    //Admin detail
    private UUID createdByUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
