package com.rentora.api.model.dto.Contract.Request;

import com.rentora.api.model.entity.Contract;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateContractRequest {
    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;


    @NotNull(message = "Rental type is required")
    private Contract.RentalType rentalType;

    //water and electric start meter
    @NotNull(message = "Water meter is required")
    private BigDecimal waterMeterStart;

    @NotNull(message = "Electricity meter is required")
    private BigDecimal electricMeterStart;


    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Rental price is required")
    @DecimalMin(value = "0.01", message = "Rental price must be greater than 0")
    private BigDecimal rentalPrice;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    @Min(value = 0, message = "Advance payment months cannot be negative")
    private Integer advancePaymentMonths = 0;




    @Size(max = 5000, message = "Terms and conditions cannot exceed 5000 characters")
    private String termsAndConditions;

    @Size(max = 2000, message = "Special conditions cannot exceed 2000 characters")
    private String specialConditions;

    private Boolean autoRenewal = false;

    @Max(value = 365, message = "Renewal notice days cannot exceed 365")
    private Integer renewalNoticeDays;

    private String documentUrl;
}