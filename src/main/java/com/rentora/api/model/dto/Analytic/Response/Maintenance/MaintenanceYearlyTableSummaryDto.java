package com.rentora.api.model.dto.Analytic.Response.Maintenance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MaintenanceYearlyTableSummaryDto {
    private Integer year;
    private Long totalRequests;
    private BigDecimal totalCost;
    private Long completed;
    private Long pending;
    private BigDecimal avgCost;
    private Double completionRate;
}
