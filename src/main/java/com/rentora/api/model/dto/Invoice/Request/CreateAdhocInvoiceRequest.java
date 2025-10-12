package com.rentora.api.model.dto.Invoice.Request;

import java.util.UUID;

import com.rentora.api.model.entity.AdhocInvoice;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class CreateAdhocInvoiceRequest {

    @NotNull(message = "Unit ID is required")
    private UUID unitId;


    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    @NotNull(message ="Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotBlank(message = "Category is required")
    private AdhocInvoice.AdhocInvoiceCategory category;

    @NotNull(message = "Final amount is required")
    @DecimalMin(value = "0.01", message = "Final amount must be greater than 0")
    private BigDecimal finalAmount;

    private AdhocInvoice.PaymentStatus paymentStatus = AdhocInvoice.PaymentStatus.unpaid;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String notes;

    private Boolean includeInMonthly = false;

    @NotNull(message = "Invoice priority is required")
    private AdhocInvoice.InvoicePriority priority;

    private AdhocInvoice.InvoiceStatus status = AdhocInvoice.InvoiceStatus.active;
}
