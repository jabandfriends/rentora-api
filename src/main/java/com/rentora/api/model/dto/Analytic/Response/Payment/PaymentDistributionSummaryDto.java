package com.rentora.api.model.dto.Analytic.Response.Payment;

import com.rentora.api.model.entity.Invoice;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDistributionSummaryDto {
    private Invoice.PaymentStatus paymentStatus;
    private Double percentagePayment;
}
