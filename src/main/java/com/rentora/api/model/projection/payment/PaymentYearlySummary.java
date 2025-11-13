package com.rentora.api.model.projection.payment;

import java.math.BigDecimal;

public interface PaymentYearlySummary {
    Integer getYear();
    Long getCount();
    BigDecimal getTotalCost();
}
