package com.rentora.api.model.dto.Payment.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentMonthlyAvenue {

    private BigDecimal monthlyRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal pendingPayment;
}
