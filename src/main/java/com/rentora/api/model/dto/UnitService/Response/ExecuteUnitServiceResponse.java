package com.rentora.api.model.dto.UnitService.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ExecuteUnitServiceResponse {
    private UUID unitServiceId;
}
