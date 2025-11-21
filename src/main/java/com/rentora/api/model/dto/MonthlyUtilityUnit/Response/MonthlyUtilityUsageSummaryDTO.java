package com.rentora.api.model.dto.MonthlyUtilityUnit.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class MonthlyUtilityUsageSummaryDTO {
    private String month;
    private BigDecimal usageAmount;
}
