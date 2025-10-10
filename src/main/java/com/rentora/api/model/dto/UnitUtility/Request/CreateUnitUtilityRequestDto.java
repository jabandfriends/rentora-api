package com.rentora.api.model.dto.UnitUtility.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateUnitUtilityRequestDto {
    @NotNull(message = "Reading Month is required")
    @Size(max = 2, message = "Reading month max size = 2")
    private LocalDate meterDate;
    private List<UnitUtility> rooms;
}
