package com.rentora.api.model.dto.Analytic.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MaintenanceSummaryDTO {
    private String period;
    private Long count;
    private BigDecimal totalCost;
}