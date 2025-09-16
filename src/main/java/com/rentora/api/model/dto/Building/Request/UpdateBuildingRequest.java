package com.rentora.api.model.dto.Building.Request;

import com.rentora.api.model.entity.Building;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBuildingRequest {
    @Size(max = 100, message = "Building name cannot exceed 100 characters")
    private String name;

    private String description;

    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;

    private Building.BuildingType buildingType;

    private Building.BuildingStatus status;
}