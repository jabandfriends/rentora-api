package com.rentora.api.model.dto.Floor.Response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FloorResponseRequestDto {
    private UUID floorId;
    private String floorName;
    private Integer floorNumber;
    private Integer totalUnits;
    private UUID buildingId;
    private String buildingName;
}
