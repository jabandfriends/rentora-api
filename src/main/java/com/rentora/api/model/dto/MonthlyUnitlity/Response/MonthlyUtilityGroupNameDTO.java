package com.rentora.api.model.dto.MonthlyUnitlity.Response;

import com.rentora.api.model.entity.UnitUtilities;
import lombok.Data;

import java.util.List;

@Data
public class MonthlyUtilityGroupNameDTO {

    private String utilityName;

    private List<MonthlyUtilityUsageSummaryDTO> monthlyUsages;
}
