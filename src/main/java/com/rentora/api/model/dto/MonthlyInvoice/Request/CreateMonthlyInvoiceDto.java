package com.rentora.api.model.dto.MonthlyInvoice.Request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateMonthlyInvoiceDto {
    private UUID unitId;
    private LocalDate readingDate;
    private Integer paymentDueDay;
}
