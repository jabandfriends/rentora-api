package com.rentora.api.model.dto.UnitService.Request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateUnitServiceRequest {

    private UUID unitId;

    private UUID serviceId;

    private String serviceName;

    private Integer quantity;

    private BigDecimal monthlyPrice;
}
