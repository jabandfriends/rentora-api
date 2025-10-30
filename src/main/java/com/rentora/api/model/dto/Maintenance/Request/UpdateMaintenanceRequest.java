package com.rentora.api.model.dto.Maintenance.Request;

import com.rentora.api.model.dto.Maintenance.Response.MaintenanceSupplyResponseDto;
import com.rentora.api.model.entity.Maintenance;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateMaintenanceRequest {
    @Size(max = 100, message = "Maintenance name cannot exceed 100 characters")
    private String title;
    private String description;

    private UUID unitId;

    private Maintenance.Category category;
    private Maintenance.Status status;
    private Maintenance.Priority priority;

    private OffsetDateTime appointmentDate;
    private OffsetDateTime dueDate;

    @DecimalMin(value = "0.0", message = "estimatedHours cannot be negative")
    private BigDecimal estimatedHours;


    @DecimalMin(value = "0.0", message = "estimatedCost cannot be negative")
    private BigDecimal estimatedCost;

    private Boolean isEmergency;
    private Boolean isRecurring;
//
    private Maintenance.RecurringSchedule recurringSchedule;

    //supply system
    private List<MaintenanceSupplyResponseDto> suppliesUsage;
}
