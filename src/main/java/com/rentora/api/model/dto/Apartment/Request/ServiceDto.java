package com.rentora.api.model.dto.Apartment.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceDto {
    @NotBlank(message = "Service name is required")
    private String name;

    @Min(value = 0, message = "Price must be zero or positive")
    private BigDecimal price;
}
