package com.rentora.api.model.dto.Maintenance.Request;

import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceSupplyUsageRequest {
    private UUID supplyId;
    private Integer supplyUsedQuantity;
}
