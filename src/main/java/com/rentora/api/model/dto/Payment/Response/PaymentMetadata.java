package com.rentora.api.model.dto.Payment.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMetadata {
    private long totalPayments;
    private long totalPaymentsComplete;
    private long totalPaymentsPending;
    private long totalPaymentsFailed;
}
