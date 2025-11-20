package com.rentora.api.model.dto.Unit.Response;

import com.rentora.api.model.entity.Unit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitDetailDto {
    private String id;
    private String unitName;
    private Unit.UnitType unitType;

    private Unit.UnitStatus unitStatus;
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