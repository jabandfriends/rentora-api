package com.rentora.api.model.dto.Maintenance.Response;

import com.rentora.api.model.entity.MaintenanceSupply;
import com.rentora.api.model.entity.Supply;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MaintenanceSupplyResponseDto {
    private UUID maintenanceSupplyId;

    private Integer supplyUsedQuantity;

    //supply info
    private UUID supplyId;
    private String supplyName;
    private String supplyDescription;
    private Supply.SupplyCategory supplyCategory;
    private BigDecimal supplyUnitPrice;
    private String supplyUnit;
}
