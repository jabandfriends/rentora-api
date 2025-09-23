package com.rentora.api.model.dto.Invoice.Response;

import com.rentora.api.model.entity.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class InvoiceDetailDTO {
    
    private String id;
    private String invoiceNumber;
    private String tenant;
    private String room;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Invoice.PaymentStatus status;

}