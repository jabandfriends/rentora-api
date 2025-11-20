package com.rentora.api.model.dto.Analytic.Response.Maintenance;

import com.rentora.api.model.entity.Maintenance;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceCategorySummaryDto {
    private Maintenance.Category category;
    private long count;
}
