package com.rentora.api.model.dto.MonthlyUnitlity.Response;

import lombok.Data;

@Data
public class MonthlyUtilityGroupInfo {
    private MonthlyUtilityGroupNameDTO water;
    private MonthlyUtilityGroupNameDTO electricity;
}
