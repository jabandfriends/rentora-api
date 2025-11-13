package com.rentora.api.model.dto.ExtraService.Response;

import com.rentora.api.model.entity.ApartmentService;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceInfoDTO {
    private UUID id;
    private String serviceName;
    private ApartmentService.Category category;
    private Boolean isActive;
    private BigDecimal price;

}
