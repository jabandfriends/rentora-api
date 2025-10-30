package com.rentora.api.model.dto.Supply.Request;

import com.rentora.api.model.entity.Supply;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSupplyRequestDto {
    @NotBlank(message = "Supply name is required")
    @Size(max = 100, message = "Supply name must be at most 100 characters")
    private String name;

    @NotNull(message = "Supply category is required")
    private Supply.SupplyCategory category;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit can be at most 20 characters")
    private String unit;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Minimum stock is required")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minStock;

    @NotNull(message = "Cost per unit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost per unit cannot be negative")
    private BigDecimal costPerUnit;
}
