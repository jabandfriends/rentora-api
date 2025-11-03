package com.rentora.api.model.dto.MonthlyUtilityBuilding.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyUtilityBuildingUsageSummary
{
    private String month;
    private BigDecimal totalUsageAmount;
}
