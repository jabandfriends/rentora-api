package com.rentora.api.model.projection.maintenance;

import java.math.BigDecimal;

public interface MaintenanceYearlySummaryProjection {
    Integer getYear();
    Long getTotalRequests();
    BigDecimal getTotalCost();
    Long getCompleted();
    Long getPending();
}

