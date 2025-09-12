package com.rentora.api.dto.Unit.Request;

import com.rentora.api.entity.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateUnitRequest {
    @Size(max = 50, message = "Unit name cannot exceed 50 characters")
    private String unitName;

    private String unitType;

    @Min(value = 0, message = "Bedrooms cannot be negative")
    private Integer bedrooms;

    @DecimalMin(value = "0.0", message = "Bathrooms cannot be negative")
    private BigDecimal bathrooms;

    @DecimalMin(value = "0.0", message = "Square meters cannot be negative")
    private BigDecimal squareMeters;

    @Min(value = 0, message = "Balcony count cannot be negative")
    private Integer balconyCount;

    @Min(value = 0, message = "Parking spaces cannot be negative")
    private Integer parkingSpaces;

    private Unit.UnitStatus status;
    private Unit.FurnishingStatus furnishingStatus;

    private String floorPlanUrl;
    private String notes;
}
