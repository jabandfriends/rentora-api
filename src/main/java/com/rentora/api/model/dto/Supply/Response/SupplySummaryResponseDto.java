package com.rentora.api.model.dto.Supply.Response;

import com.rentora.api.model.entity.Supply;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SupplySummaryResponseDto {
    private UUID supplyId;
    private String supplyName;
    private Integer supplyQuantity;
    private Integer supplyMinStock;
    private BigDecimal supplyUnitPrice;
    private String supplyUnit;
    private Supply.SupplyCategory supplyCategory;
    private Supply.SupplyStockStatus supplyStockStatus;
    private BigDecimal supplyTotalCost;
}
