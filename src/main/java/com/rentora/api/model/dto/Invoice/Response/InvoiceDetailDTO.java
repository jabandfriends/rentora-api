package com.rentora.api.model.dto.Invoice.Response;

import com.rentora.api.model.entity.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class InvoiceDetailDTO {

//Overview
    private String id;
    private String invoiceNumber;
    private String contract;
    private Invoice.PaymentStatus status;

//Amount
    private BigDecimal rentalAmount;
    private BigDecimal utilAmount;
    private BigDecimal serviceAmount;
    private BigDecimal feesAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

//Date
    private LocalDate billStart;
    private LocalDate dueDate;

//Property
    private String apartment;
    private String unit;
    private String room;

//Tenant
    private String tenant;
    private String email;

//Document
    private String pdf;

//Note
    private String notes;

//Detail
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}