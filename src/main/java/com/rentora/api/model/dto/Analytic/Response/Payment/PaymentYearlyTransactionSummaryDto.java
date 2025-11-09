package com.rentora.api.model.dto.Analytic.Response.Payment;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentYearlyTransactionSummaryDto {
    private Integer period;
    private Long count;
}
