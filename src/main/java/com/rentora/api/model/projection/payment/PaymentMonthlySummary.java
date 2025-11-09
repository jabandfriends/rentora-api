package com.rentora.api.model.projection.payment;

import java.math.BigDecimal;

public interface PaymentMonthlySummary {
    Integer getMonth();      // from MONTH(m.requestedDate)
    Long getCount();         // from COUNT(m)
    BigDecimal getTotalCost(); // from SUM(m.actualCost)
}
