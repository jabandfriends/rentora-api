package com.rentora.api.model.dto.ApartmentService.Request;

import com.rentora.api.model.entity.ApartmentService;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApartmentServiceCreateRequestDto {
    private String serviceName;
    private ApartmentService.Category category;
    private BigDecimal price;
    private Boolean isActive;
}
