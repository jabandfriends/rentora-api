package com.rentora.api.model.dto.ApartmentUtility.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApartmentUtilityMonthlyUsage {
    private String month;
    private BigDecimal usageAmount;
}
