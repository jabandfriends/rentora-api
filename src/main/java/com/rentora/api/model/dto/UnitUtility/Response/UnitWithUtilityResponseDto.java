package com.rentora.api.model.dto.UnitUtility.Response;

import com.rentora.api.model.entity.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UnitWithUtilityResponseDto {
    UUID unitId;
    String unitName;
    String buildingName;
    Unit.UnitStatus unitStatus;
    BigDecimal waterMeterStart;
    BigDecimal electricMeterStart;
}
