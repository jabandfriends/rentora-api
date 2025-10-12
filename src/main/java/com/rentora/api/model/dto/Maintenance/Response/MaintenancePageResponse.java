package com.rentora.api.model.dto.Maintenance.Response;

import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import lombok.Data;
import java.util.List;

@Data
public class MaintenancePageResponse {
    private List<MaintenanceDetailDTO> maintenances;
    private long totalMaintenance;
    private long pendingCount;
    private long assignedCount;
    private long inProgressCount;
    // Add other counts as needed, like completedCount or cancelledCount
    private int currentPage;
    private int totalPages;
}