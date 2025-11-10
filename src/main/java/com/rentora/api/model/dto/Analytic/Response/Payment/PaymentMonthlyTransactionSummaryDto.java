package com.rentora.api.model.dto.Analytic.Response.Payment;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PaymentMonthlyTransactionSummaryDto {
    private String period;
    private Long count;
}
