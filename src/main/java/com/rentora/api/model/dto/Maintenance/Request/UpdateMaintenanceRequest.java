package com.rentora.api.model.dto.Maintenance.Request;

import com.rentora.api.model.entity.Maintenance;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class UpdateMaintenanceRequest {
    @Size(max = 100, message = "Maintenance name cannot exceed 100 characters")
    private String title;

    private String description;

//    private Maintenance.Category category;
//    private Maintenance.Status status;
    private Maintenance.Priority priority;

    private OffsetDateTime appointmentDate;
    private OffsetDateTime dueDate;

    @DecimalMin(value = "0.0", message = "estimatedHours cannot be negative")
    private BigDecimal estimatedHours;

//    @DecimalMin(value = "0.0", message = "actualHours cannot be negative")
//    private BigDecimal actualHours;
//
//    @DecimalMin(value = "0.0", message = "estimatedCost cannot be negative")
//    private BigDecimal estimatedCost;
//
//    @DecimalMin(value = "0.0", message = "actualCost cannot be negative")
//    private BigDecimal actualCost;
//
//    private String workSummary;
//
//    private Boolean isEmergency;
//    private Boolean isRecurring;
//
//    private Maintenance.RecurringSchedule recurringSchedule;
}
