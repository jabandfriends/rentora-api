package com.rentora.api.model.dto.SupplyTransaction.Response;

import com.rentora.api.model.entity.SupplyTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplyTransactionSummaryResponseDto {
    private LocalDateTime transactionDate;
    private String supplyName;
    private SupplyTransaction.SupplyTransactionType supplyTransactionType;
    private Integer quantity;
    private String note;
}
