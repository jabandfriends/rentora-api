package com.rentora.api.model.dto.Invoice.Request;

import com.rentora.api.model.entity.AdhocInvoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AdhocInvoiceUpdateRequestDto {
    private UUID invoiceId;
    private String title;
    private String description;
    private AdhocInvoice.AdhocInvoiceCategory category;
    private BigDecimal amount;
    private LocalDate dueDate;
    private AdhocInvoice.PaymentStatus paymentStatus;
    private AdhocInvoice.InvoiceStatus invoiceStatus;
    private AdhocInvoice.InvoicePriority invoicePriority;
    private String fileName;
}
