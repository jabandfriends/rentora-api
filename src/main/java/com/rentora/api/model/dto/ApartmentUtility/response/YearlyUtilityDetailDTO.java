package com.rentora.api.model.dto.ApartmentUtility.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class YearlyUtilityDetailDTO {

    private int year;
    private Map<String, BigDecimal> usageTotals;
}