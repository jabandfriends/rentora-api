package com.rentora.api.model.dto.Maintenance.Request;

import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.MaintenanceSupply;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMaintenanceRequest {

    @NotNull(message = "unitId is required")
    private UUID unitId; // Add this line

    @NotBlank(message = "Maintenance name is required")
    @Size(max = 100, message = "Maintenance name cannot exceed 100 characters")
    private String title;

    private String description;

    private Maintenance.Category category;
    private Maintenance.Status status;
    private Maintenance.Priority priority;

    private OffsetDateTime appointmentDate;
    private OffsetDateTime dueDate;
    private OffsetDateTime startAt;
    private OffsetDateTime completedAt;

    @DecimalMin(value = "0.0", message = "estimatedHours cannot be negative")
    private BigDecimal estimatedHours;

    @DecimalMin(value = "0.0", message = "actualHours cannot be negative")
    private BigDecimal actualHours;

    @DecimalMin(value = "0.0", message = "estimatedCost cannot be negative")
    private BigDecimal estimatedCost;

    @DecimalMin(value = "0.0", message = "actualCost cannot be negative")
    private BigDecimal actualCost;

    private String workSummary;

    private Boolean isEmergency;
    private Boolean isRecurring;

    private Maintenance.RecurringSchedule recurringSchedule;

    //supply system
    private List<MaintenanceSupplyUsageRequest> suppliesUsage;

}


