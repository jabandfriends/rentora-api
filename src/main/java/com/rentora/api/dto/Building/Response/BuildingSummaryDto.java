package com.rentora.api.dto.Building.Response;

import com.rentora.api.entity.Building;
import lombok.Data;

@Data
public class BuildingSummaryDto {
    private String id;
    private String name;
    private String description;
    private Integer totalFloors;
    private Building.BuildingType buildingType;
    private Building.BuildingStatus status;
    private String apartmentName;
    private String createdAt;

    // Floor and unit counts
    private Long floorCount;
    private Long unitCount;
}