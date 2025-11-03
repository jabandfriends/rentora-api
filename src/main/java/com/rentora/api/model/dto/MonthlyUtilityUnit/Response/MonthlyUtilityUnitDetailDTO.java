package com.rentora.api.model.dto.MonthlyUtilityUnit.Response;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class MonthlyUtilityUnitDetailDTO {
    //Unit
    private UUID unitId;
    private String unitName;
    private Integer floorNumber;
    private String buildingName;

    private Map<String, List<MonthlyUtilityUsageSummaryDTO>> utilityGroupName;

    // private MonthlyUtilityGroupInfo utilityGroups;

}
