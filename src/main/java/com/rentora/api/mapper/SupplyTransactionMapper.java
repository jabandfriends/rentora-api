package com.rentora.api.mapper;


import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.SupplyTransaction;
import org.springframework.stereotype.Component;

@Component
public class SupplyTransactionMapper {

    public SupplyTransactionSummaryResponseDto supplyTransactionSummaryResponseDto(SupplyTransaction supplyTransaction) {
        return SupplyTransactionSummaryResponseDto.builder()
                .transactionDate(supplyTransaction.getCreatedAt())
                .supplyName(supplyTransaction.getSupply().getName())
                .supplyTransactionType(supplyTransaction.getTransactionType())
                .quantity(supplyTransaction.getQuantity())
                .note(supplyTransaction.getNote())
                .build();
    }
}
