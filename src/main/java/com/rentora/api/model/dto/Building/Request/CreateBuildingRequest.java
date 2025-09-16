package com.rentora.api.model.dto.Building.Request;


import com.rentora.api.model.entity.Building;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Data
public class CreateBuildingRequest {
    @NotNull(message = "Apartment ID is required")
    private UUID apartmentId;

    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name cannot exceed 100 characters")
    private String name;

    private String description;

    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;

    private Building.BuildingType buildingType = Building.BuildingType.residential;
}