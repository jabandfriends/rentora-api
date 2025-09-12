package com.rentora.api.dto.Unit.Response;

import com.rentora.api.entity.Unit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitSummaryDto {
    private String id;
    private String unitName;
    private String unitType;
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private BigDecimal squareMeters;
    private Unit.UnitStatus status;
    private Unit.FurnishingStatus furnishingStatus;
    private String floorName;
    private String buildingName;
    private String apartmentName;
    private String currentTenant;
    private String createdAt;
}