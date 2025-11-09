package com.rentora.api.model.dto.Analytic.Response.Payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentStatsSummaryDto {
    BigDecimal totalRental;
    BigDecimal totalPaid;
    BigDecimal totalPending;
    BigDecimal totalOverdue;
}
