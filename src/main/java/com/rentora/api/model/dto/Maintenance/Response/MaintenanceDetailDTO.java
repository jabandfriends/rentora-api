package com.rentora.api.model.dto.Maintenance.Response;

import com.rentora.api.model.entity.Maintenance;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Data
public class MaintenanceDetailDTO {
    private UUID id;
    private String ticketNumber;

    //Unit
    private String unitName;

    //tenant
    private String tenantName;
    private String tenantEmail;
    private String tenantPhoneNumber;

    //building
    private String buildingsName;

    private String title;
    private String description;
    private Maintenance.Category category;
    private Maintenance.Status status;
    private Maintenance.Priority priority;
    private LocalDate requestedDate;
    private OffsetDateTime appointmentDate;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime dueDate;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String workSummary;
    private String assignedToUserName;

    private String tenantFeedback;
    private Integer tenantRating;

    private Boolean isEmergency;
    private Boolean isRecurring;


    private Maintenance.RecurringSchedule recurringSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
