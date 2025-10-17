package com.rentora.api.model.dto.Report.Metadata;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportUnitUtilityMetadata {
    private long electricUsageUnits;
    private long waterUsageUnits;
    private long totalUsageUnits;
    private BigDecimal electricUsagePrices;
    private BigDecimal waterUsagePrices;
    private BigDecimal totalAmount;
}
