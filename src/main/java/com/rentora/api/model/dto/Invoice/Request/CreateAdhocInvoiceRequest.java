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

    @NotNull(message = "Apartment ID is required")
    private UUID apartment;

    @NotBlank(message = "Adhoc number is required")
    private String adhocNumber;

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    @NotNull(message ="Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Final amount is required")
    @DecimalMin(value = "0.01", message = "Final amount must be greater than 0")
    private BigDecimal finalAmount;

    @NotNull(message = "Payment status is required")
    private AdhocInvoice.PaymentStatus paymentStatus;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String notes;

    private Boolean includeInMonthly = false;

    @NotNull(message = "Invoice priority is required")
    private AdhocInvoice.InvoicePriority priority;

    @NotNull(message = "Invoice status is required")
    private AdhocInvoice.InvoiceStatus status;
}
