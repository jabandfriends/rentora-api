package com.rentora.api.model.dto.MonthlyUtilityBuilding.Response;

import com.rentora.api.model.dto.MonthlyUtilityUnit.Response.MonthlyUtilityUsageSummaryDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class MonthlyUtilityBuildingDetailDTO {
    private UUID buildingID;
    private String buildingName;

    private Map<String, List<MonthlyUtilityBuildingUsageSummary>> utilityGroupName;
}
