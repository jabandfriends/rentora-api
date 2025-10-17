package com.rentora.api.model.dto.UnitService.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateUnitServiceRequest {

    @NotNull(message = "unitId is required")
    private UUID unitId;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private Integer quantity;

    private BigDecimal monthlyPrice;
}
