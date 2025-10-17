package com.rentora.api.model.dto.Utility.Request;

import com.rentora.api.model.entity.Utility;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UpdateUtilityDto {
    private UUID waterUtilityId;
    private Utility.UtilityType waterUtilityType;
    private BigDecimal waterUtilityUnitPrice;
    private BigDecimal waterUtilityFixedPrice;


    private UUID electricUtilityId;
    private Utility.UtilityType electricUtilityType;
    private BigDecimal electricUtilityUnitPrice;
    private BigDecimal electricUtilityFixedPrice;
}
