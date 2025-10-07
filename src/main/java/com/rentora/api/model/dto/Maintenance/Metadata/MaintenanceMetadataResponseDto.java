package com.rentora.api.model.dto.Maintenance.Metadata;

import lombok.Data;

@Data
public class MaintenanceMetadataResponseDto {
    private Integer totalMaintenance;
    private long pendingCount;
    private long assignedCount;
    private long inProgressCount;
}
