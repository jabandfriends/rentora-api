package com.rentora.api.model.projection.maintenance;

import com.rentora.api.model.entity.Maintenance;

public interface MaintenanceCategorySummaryProjection {
    Maintenance.Category getCategory();
    Long getCount();
}
