package com.rentora.api.model.dto.Maintenance.Request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MaintenanceSupplyUsageRequest {
    private UUID supplyId;
    private Integer supplyUsedQuantity;
}
