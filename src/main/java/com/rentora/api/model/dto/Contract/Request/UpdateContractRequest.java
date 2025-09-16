package com.rentora.api.model.dto.Contract.Request;

import com.rentora.api.model.entity.Contract;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateContractRequest {
    @Size(max = 100, message = "Guarantor name cannot exceed 100 characters")
    private String guarantorName;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 15, message = "Guarantor phone cannot exceed 15 characters")
    private String guarantorPhone;

    @Size(max = 20, message = "Guarantor ID number cannot exceed 20 characters")
    private String guarantorIdNumber;

    private LocalDate endDate;

    @DecimalMin(value = "0.01", message = "Rental price must be greater than 0")
    private BigDecimal rentalPrice;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    @Min(value = 0, message = "Advance payment months cannot be negative")
    private Integer advancePaymentMonths;

    @DecimalMin(value = "0.0", message = "Late fee amount cannot be negative")
    private BigDecimal lateFeeAmount;

    private Boolean utilitiesIncluded;

    @Size(max = 5000, message = "Terms and conditions cannot exceed 5000 characters")
    private String termsAndConditions;

    @Size(max = 2000, message = "Special conditions cannot exceed 2000 characters")
    private String specialConditions;

    private Boolean autoRenewal;

    @Min(value = 1, message = "Renewal notice days must be at least 1")
    @Max(value = 365, message = "Renewal notice days cannot exceed 365")
    private Integer renewalNoticeDays;

    private String documentUrl;

    private Contract.ContractStatus status;
}
