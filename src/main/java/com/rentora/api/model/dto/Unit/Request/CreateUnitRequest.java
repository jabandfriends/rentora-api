package com.rentora.api.model.dto.Unit.Request;

import com.rentora.api.model.entity.Unit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateUnitRequest {
    @NotNull(message = "Floor ID is required")
    private UUID floorId;

    @NotBlank(message = "Unit name is required")
    @Size(max = 50, message = "Unit name cannot exceed 50 characters")
    private String unitName;

    private Unit.UnitType unitType = Unit.UnitType.APARTMENT;

    @Min(value = 0, message = "Bedrooms cannot be negative")
    private Integer bedrooms = 1;

    @DecimalMin(value = "0.0", message = "Bathrooms cannot be negative")
    private BigDecimal bathrooms = BigDecimal.ONE;

    @DecimalMin(value = "0.0", message = "Square meters cannot be negative")
    private BigDecimal squareMeters;

    @Min(value = 0, message = "Balcony count cannot be negative")
    private Integer balconyCount = 0;

    @Min(value = 0, message = "Parking spaces cannot be negative")
    private Integer parkingSpaces = 0;

    private Unit.FurnishingStatus furnishingStatus = Unit.FurnishingStatus.unfurnished;

    private String floorPlanUrl;
    private String notes;
}