package com.rentora.api.model.dto.MonthlyUtilityUnit.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyUtilityUsageSummaryDTO {
    private String month;
    private BigDecimal usageAmount;
}
