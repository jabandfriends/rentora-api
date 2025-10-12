package com.rentora.api.model.dto.UnitService.Request;

import com.rentora.api.model.entity.ServiceEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
