package com.rentora.api.model.dto.Maintenance.Response;

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
    private LocalDate requestedDate;
    private LocalDate appointmentDate;
    private String status;
}
