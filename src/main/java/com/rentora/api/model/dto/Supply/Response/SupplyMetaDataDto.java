package com.rentora.api.model.dto.Supply.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SupplyMetaDataDto {
    private long totalSupplies;
    private long totalLowStockSupplies;
    private BigDecimal totalCostSupplies;
}
