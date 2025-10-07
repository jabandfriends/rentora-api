package com.rentora.api.model.dto.Maintenance.Response;

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
    private String category;
    private String status;
    private String priority;
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
    private String assignedToUserName;

    private String tenantFeedback;
    private Integer tenantRating;

    private Boolean isEmergency;
    private Boolean isRecurring;

    private String recurringSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
