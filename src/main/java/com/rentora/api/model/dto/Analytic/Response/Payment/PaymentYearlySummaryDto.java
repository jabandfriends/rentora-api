package com.rentora.api.model.dto.Analytic.Response.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentYearlySummaryDto {
    private Integer period;
    private Long count;
    private BigDecimal totalCost;
}
