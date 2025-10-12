package com.rentora.api.model.dto.MonthlyInvoice.Request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateMonthlyInvoiceDto {
    private UUID unitId;
    private Integer readingMonth;
    private Integer readingYear;
    private Integer paymentDueDay;
}
