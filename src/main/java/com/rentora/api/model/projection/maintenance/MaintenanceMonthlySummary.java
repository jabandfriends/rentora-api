package com.rentora.api.model.projection.maintenance;

import java.math.BigDecimal;

public interface MaintenanceMonthlySummary {
    Integer getMonth();      // from MONTH(m.requestedDate)
    Long getCount();         // from COUNT(m)
    BigDecimal getTotalCost(); // from SUM(m.actualCost)
}
