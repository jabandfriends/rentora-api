package com.rentora.api.model.projection.maintenance;

import java.math.BigDecimal;

public interface MaintenanceYearlySummary {
    Integer getYear();       // matches AS year
    Long getCount();         // matches AS count
    BigDecimal getTotalCost(); // matches AS totalCost
}