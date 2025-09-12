package com.rentora.api.dto.Unit.Response;

import com.rentora.api.entity.Unit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitDetailDto {
    private String id;
    private String unitName;
    private Unit.UnitType unitType;
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private BigDecimal squareMeters;
    private Integer balconyCount;
    private Integer parkingSpaces;
    private Unit.UnitStatus status;
    private Unit.FurnishingStatus furnishingStatus;
    private String floorPlanUrl;
    private String notes;
    private String floorId;
    private String floorName;
    private Integer floorNumber;
    private String buildingId;
    private String buildingName;
    private String apartmentId;
    private String apartmentName;
    private String createdAt;
    private String updatedAt;

    // Current contract info
    private String currentContractId;
    private String currentTenantId;
    private String currentTenantName;
    private String currentTenantEmail;
    private BigDecimal currentRentalPrice;
}