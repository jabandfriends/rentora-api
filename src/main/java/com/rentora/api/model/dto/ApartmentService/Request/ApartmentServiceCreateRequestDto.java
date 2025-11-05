package com.rentora.api.model.dto.ApartmentService.Request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApartmentCreateRequestDto {
    private String serviceName;
    private BigDecimal price;
}
