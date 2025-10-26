package com.rentora.api.mapper;


import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.SupplyTransaction;
import org.springframework.stereotype.Component;

@Component
public class SupplyTransactionMapper {

    public SupplyTransactionSummaryResponseDto supplyTransactionSummaryResponseDto(SupplyTransaction supplyTransaction) {
        String quantityText;

        if (supplyTransaction.getTransactionType() == SupplyTransaction.SupplyTransactionType.adjustment ||
        supplyTransaction.getTransactionType() == SupplyTransaction.SupplyTransactionType.use) {
            quantityText = "-" + supplyTransaction.getQuantity();
        } else {
            quantityText = "+" + supplyTransaction.getQuantity();
        }

        return SupplyTransactionSummaryResponseDto.builder()
                .transactionDate(supplyTransaction.getCreatedAt())
                .supplyName(supplyTransaction.getSupply().getName())
                .supplyTransactionType(supplyTransaction.getTransactionType())
                .quantity(quantityText)
                .note(supplyTransaction.getNote())
                .changeByUser(supplyTransaction.getApartmentUser().getUser().getFullName())
                .build();
    }
}
