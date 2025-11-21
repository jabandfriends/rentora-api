package com.rentora.api.model.dto.MonthlyUtilityFloor.Respose;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyUtilityFloorUsageSummary {
    private String month;
    private BigDecimal totalFloorUsage;
}
