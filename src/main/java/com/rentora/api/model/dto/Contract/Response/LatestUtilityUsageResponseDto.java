package com.rentora.api.model.dto.Contract.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LatestUtilityUsageResponseDto {
    private String utilityType; //water electric
    private LocalDate readingDate;
    private BigDecimal totalCost;
    private BigDecimal beforeReading;
    private BigDecimal afterReading;
    private BigDecimal totalUsage; //unit
}
