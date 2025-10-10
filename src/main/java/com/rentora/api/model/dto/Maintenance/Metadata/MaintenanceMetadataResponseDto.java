package com.rentora.api.model.dto.Maintenance.Metadata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceMetadataResponseDto {
    private long totalMaintenance;
    private long pendingCount;
    private long completedCount;
    private long inProgressCount;
}
