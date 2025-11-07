package com.rentora.api.model.dto.MonthlyUtilityFloor.Respose;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class MonthlyUtilityFloorDetailDto {
    private String FloorName;
    private Integer FloorNumber;
    private String BuildingName;
    private UUID buildingId;
    private Map<String, List<MonthlyUtilityFloorUsageSummary>> utilityGroupName;
}
