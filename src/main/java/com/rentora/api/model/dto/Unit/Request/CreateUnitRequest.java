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

    private String unitName;

    private Unit.UnitType unitType = Unit.UnitType.apartment;

    private String floorPlanUrl;
    private String notes;
}