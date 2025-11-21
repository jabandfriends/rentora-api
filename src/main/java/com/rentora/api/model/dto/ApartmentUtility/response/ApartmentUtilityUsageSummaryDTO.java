package com.rentora.api.model.dto.ApartmentUtility.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ApartmentUtilityUsageSummaryDTO {
    private UUID apartmentId;

    // totalUsage all year
    private Map<String, BigDecimal> totalUsage;

    // List of each month
    private Map<String, List<ApartmentUtilityMonthlyUsage>> monthlyBreakdown;
}
