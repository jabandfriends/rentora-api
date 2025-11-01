package com.rentora.api.model.dto.Unit.Request;

import com.rentora.api.model.entity.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateUnitRequest {
    @Size(max = 50, message = "Unit name cannot exceed 50 characters")
    private String unitName;

    private Unit.UnitType unitType;

    private Unit.UnitStatus status;

    private String floorPlanUrl;
    private String notes;
}
