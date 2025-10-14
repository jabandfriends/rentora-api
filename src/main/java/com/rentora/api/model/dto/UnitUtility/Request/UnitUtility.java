package com.rentora.api.model.dto.UnitUtility.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UnitUtility {
    @NotNull
    private UUID unitId;

    private String unitName;

    @Min(value = 0,message = "Water meter can not be negative")
    @Max(value = 9999)
    private Long waterStart;

    @Min(value = 0,message = "Water meter can not be negative")
    @Max(value = 9999)
    private Long waterEnd;

    @Min(value = 0,message = "Electricity meter can not be negative")
    @Max(value = 9999)
    private Long electricStart;

    @Min(value = 0,message = "Electricity meter can not be negative")
    @Max(value = 9999)
    private Long electricEnd;
}
