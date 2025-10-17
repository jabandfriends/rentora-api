package com.rentora.api.model.dto.Unit.Response;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UnitSummaryDto {
    private String id;
    private String unitName;
    private Unit.UnitType unitType;
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private BigDecimal squareMeters;
    private Unit.UnitStatus unitStatus;
    private Unit.FurnishingStatus furnishingStatus;
    private String floorName;
    private String buildingName;
    private String apartmentName;
    private String currentTenant;
    private String createdAt;


    private String contractNumber;
    private Contract.RentalType rentalType;
    private Contract.ContractStatus contractStatus;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;

    private Integer balconyCount;
    private Integer parkingSpaces = 0;
}