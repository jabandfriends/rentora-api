package com.rentora.api.model.dto.Apartment.Request;


import com.rentora.api.model.entity.Utility;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.math.BigDecimal;
import java.util.List;

@Data
public class SetupApartmentRequest {

    @NotBlank(message = "Bank account holder is required")
    private String bankAccountHolder;

    @NotBlank(message="Bank account number is required")
    @Pattern(regexp = "\\d+", message = "Bank account number must be numeric")
    private String bankAccountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotEmpty(message = "At least one building is required")
    private List<BuildingDto> buildings;

    private BigDecimal electricityFlat;

    private BigDecimal electricityPrice;

    private Utility.UtilityType electricityType;

    @NotEmpty(message = "At least one service is required")
    private List<ServiceDto> services;

    private BigDecimal waterFlat;

    private BigDecimal waterPrice;

    private Utility.UtilityType waterType;

}
