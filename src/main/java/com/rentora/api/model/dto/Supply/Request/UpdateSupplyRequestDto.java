package com.rentora.api.model.dto.Supply.Request;

import com.rentora.api.model.entity.Supply;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UpdateSupplyRequestDto {
    @Size(max = 100, message = "Supply name must be at most 100 characters")
    private String name;

    private Supply.SupplyCategory category;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @Size(max = 20, message = "Unit can be at most 20 characters")
    private String unit;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minStock;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost per unit cannot be negative")
    private BigDecimal costPerUnit;
}
