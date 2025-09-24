package com.rentora.api.model.dto.Maintenance.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ExecuteMaintenanceResponse {
    private UUID maintenanceId;
}
