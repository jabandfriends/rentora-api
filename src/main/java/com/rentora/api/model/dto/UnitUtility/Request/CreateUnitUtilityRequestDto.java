package com.rentora.api.model.dto.UnitUtility.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateUnitUtilityRequestDto {
    private Integer readingMonth;
    private Integer readingYear;
    private List<UnitUtility> rooms;
}
