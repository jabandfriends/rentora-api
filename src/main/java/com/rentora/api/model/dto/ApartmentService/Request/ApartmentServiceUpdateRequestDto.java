package com.rentora.api.model.dto.ApartmentService.Request;

import com.rentora.api.model.entity.ApartmentService;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApartmentServiceUpdateRequestDto {
    private UUID apartmentServiceId;
    private String serviceName;
    private BigDecimal price;
    private ApartmentService.Category category;
    private Boolean isActive;
}
