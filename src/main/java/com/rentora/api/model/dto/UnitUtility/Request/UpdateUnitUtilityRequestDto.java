package com.rentora.api.model.dto.UnitUtility.Request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateUnitUtilityRequestDto {
    private UUID waterUnitUtilityId;
    private BigDecimal waterStart;
    private BigDecimal waterEnd;

    private UUID electricUnitUtilityId;
    private BigDecimal electricStart;
    private BigDecimal electricEnd;
}
