package com.rentora.api.dto.Contract.Response;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RenewContractRequest {
    @NotNull(message = "New end date is required")
    private LocalDateTime newEndDate;

    @DecimalMin(value = "0.01", message = "New rental price must be greater than 0")
    private BigDecimal newRentalPrice;

    @Size(max = 2000, message = "Renewal notes cannot exceed 2000 characters")
    private String renewalNotes;

    private Boolean updateTerms = false;

    @Size(max = 5000, message = "New terms and conditions cannot exceed 5000 characters")
    private String newTermsAndConditions;
}
