package com.rentora.api.model.dto.Invoice.Response;

import com.rentora.api.model.entity.AdhocInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AdhocInvoiceSummaryDTO {

    private UUID id;
    private String title;
    private String description;
    private String invoiceNumber;
    private String tenant;
    private String room;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private AdhocInvoice.PaymentStatus status;

}
