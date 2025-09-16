package com.rentora.api.model.dto.Apartment.Request;

import com.rentora.api.model.entity.Apartment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateApartmentRequest {
    @Size(max = 100, message = "Apartment name cannot exceed 100 characters")
    private String name;

    private String logoUrl;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phoneNumber;

    @Size(max = 13, message = "Tax ID cannot exceed 13 characters")
    private String taxId;

    @Min(value = 1, message = "Payment due day must be between 1 and 31")
    @Max(value = 31, message = "Payment due day must be between 1 and 31")
    private Integer paymentDueDay;

    @DecimalMin(value = "0.0", message = "Late fee cannot be negative")
    private BigDecimal lateFee;

    private Apartment.LateFeeType lateFeeType;

    @Min(value = 0, message = "Grace period days cannot be negative")
    private Integer gracePeriodDays;

    private String address;
    private String city;
    private String state;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    private String country;
    private String timezone;
    private String currency;
}
