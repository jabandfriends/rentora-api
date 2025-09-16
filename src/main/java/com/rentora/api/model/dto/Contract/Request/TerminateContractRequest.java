package com.rentora.api.model.dto.Contract.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TerminateContractRequest {
    @NotNull(message = "Termination date is required")
    private LocalDate terminationDate;

    @NotBlank(message = "Termination reason is required")
    @Size(max = 1000, message = "Termination reason cannot exceed 1000 characters")
    private String terminationReason;
}
