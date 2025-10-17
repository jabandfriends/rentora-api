package com.rentora.api.model.dto.UnitUtility.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AvailableYearsDto {
    private List<Integer> years;
}
