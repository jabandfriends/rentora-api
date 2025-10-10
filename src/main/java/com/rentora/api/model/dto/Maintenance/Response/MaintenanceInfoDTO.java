package com.rentora.api.model.dto.Maintenance.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class MaintenanceInfoDTO {
    private UUID id;
    private String ticketNumber;
    private String unitName;
    private String buildingsName;
    private String title;
    private OffsetDateTime appointmentDate;
    private OffsetDateTime dueDate;
    private String status;
}
