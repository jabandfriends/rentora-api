package com.rentora.api.model.dto.Apartment.Request;

import com.rentora.api.model.entity.Apartment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CreateApartmentRequest {
    @NotBlank(message = "Apartment name is required")
    @Size(max = 100, message = "Apartment name cannot exceed 100 characters")
    private String name;

    private String logoFileName;


    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phoneNumber;

    @Size(max = 13, message = "Tax ID cannot exceed 13 characters")
    private String taxId;

    @Min(value = 1, message = "Payment due day must be between 1 and 31")
    @Max(value = 31, message = "Payment due day must be between 1 and 31")
    private Integer paymentDueDay = 30;

    @DecimalMin(value = "0.0", message = "Late fee cannot be negative")
    private BigDecimal lateFee = BigDecimal.ZERO;

    private Apartment.LateFeeType lateFeeType = Apartment.LateFeeType.fixed;

    @Min(value = 0, message = "Grace period days cannot be negative")
    private Integer gracePeriodDays = 3;

    private String address;
    private String city;
    private String state;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    private String country = "Thailand";
    private String timezone = "Asia/Bangkok";
    private String currency = "THB";

}