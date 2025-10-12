package com.rentora.api.model.dto.ExtraService.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceInfoDTO {
    private UUID id;
    private String serviceName;
    private BigDecimal price;

}
