package com.rentora.api.model.dto.Apartment.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingDto {
    @NotBlank(message = "Building name is required.")
    private String buildingName;

    @Min(value = 1, message = "Total floor must be at least 1.")
    private Integer totalFloors;
}
