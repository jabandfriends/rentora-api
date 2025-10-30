package com.rentora.api.model.dto.Utility.Response;


import com.rentora.api.model.entity.Utility;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UtilitySummaryResponseDto {
    private UUID utilityId;
    private Utility.UtilityType utilityType;
    private String utilityName;
    private BigDecimal utilityFixedPrice;
    private BigDecimal utilityUnitPrice;
}
