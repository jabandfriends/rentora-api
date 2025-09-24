package com.rentora.api.model.dto.Maintenance.Response;

import com.rentora.api.model.entity.Maintenance;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Data
public class MaintenanceDetailDTO {
    private UUID id;
    private String ticketNumber;
    private UUID unitId;
    private UUID tenantUserId;
    private UUID assignedToUserId;
    private String title;
    private String description;
    private Maintenance.Category category;
    private Maintenance.Status status;
    private Maintenance.Priority priority;
    private LocalDate requestedDate;
    private LocalDate appointmentDate;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private LocalDate dueDate;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String workSummary;
    private String tenantFeedback;
    private Integer tenantRating;
    private Boolean isEmergency;
    private Boolean isRecurring;
    private String recurringSchedule;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
