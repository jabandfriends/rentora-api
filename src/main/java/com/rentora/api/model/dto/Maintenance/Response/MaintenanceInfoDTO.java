package com.rentora.api.model.dto.Maintenance.Response;

import com.rentora.api.model.entity.Maintenance;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceInfoDTO {
    private UUID id;
    private String ticketNumber;
    private String unitName;
    private String buildingsName;
    private String title;
    private LocalDate appointmentDate;
    private LocalDate dueDate;
    private Maintenance.Status status;
    private Maintenance.Priority priority;
}
