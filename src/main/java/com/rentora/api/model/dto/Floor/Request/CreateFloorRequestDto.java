package com.rentora.api.model.dto.Floor.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateFloorRequestDto {
    @NotNull(message = "Building id is required.")
    private UUID buildingId;

    @Min(value = 1,message = "Floor number is required or at least 1.")
    private Integer floorNumber;

    private String floorName;

    @Min(value = 1,message = "Total units is at least 1")
    private Integer totalUnits = 0;

}
