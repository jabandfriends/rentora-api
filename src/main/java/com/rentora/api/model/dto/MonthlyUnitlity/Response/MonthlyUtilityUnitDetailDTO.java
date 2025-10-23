package com.rentora.api.model.dto.MonthlyUnitlity.Response;

import com.rentora.api.model.entity.Floor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MonthlyUtilityUnitDetailDTO {
    //Unit
    private UUID unitId;
    private String unitName;
    private Integer floorNumber;
    private String buildingName;

    private MonthlyUtilityGroupInfo utilityGroups;

}
