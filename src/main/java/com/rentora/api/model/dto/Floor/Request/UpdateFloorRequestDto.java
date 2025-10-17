package com.rentora.api.model.dto.Floor.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateFloorRequestDto {
    private UUID buildingId;
    private Integer floorNumber;
    private String floorName;
    private Integer totalUnits;

}
