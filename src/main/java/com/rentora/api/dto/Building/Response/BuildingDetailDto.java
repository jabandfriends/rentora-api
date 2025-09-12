package com.rentora.api.dto.Building.Response;

import com.rentora.api.entity.Building;
import lombok.Data;

@Data
public class BuildingDetailDto {
    private String id;
    private String name;
    private String description;
    private Integer totalFloors;
    private Building.BuildingType buildingType;
    private Building.BuildingStatus status;
    private String apartmentId;
    private String apartmentName;
    private String createdAt;
    private String updatedAt;

    // Statistics
    private Long floorCount;
    private Long unitCount;
    private Long availableUnits;
    private Long occupiedUnits;
}