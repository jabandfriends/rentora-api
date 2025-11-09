package com.rentora.api.model.dto.Analytic.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MaintenanceYearlySummaryDto {
    private Integer period;
    private Long count;
    private BigDecimal totalCost;
}
