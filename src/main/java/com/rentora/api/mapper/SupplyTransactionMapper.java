package com.rentora.api.mapper;


import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.SupplyTransaction;
import org.springframework.stereotype.Component;

@Component
public class SupplyTransactionMapper {

    public SupplyTransactionSummaryResponseDto supplyTransactionSummaryResponseDto(SupplyTransaction supplyTransaction) {

        Maintenance maintenance =  supplyTransaction.getMaintenance();

        SupplyTransactionSummaryResponseDto supplyTransactionSummary = SupplyTransactionSummaryResponseDto.builder()
                .transactionDate(supplyTransaction.getCreatedAt())
                .supplyName(supplyTransaction.getSupply().getName())
                .supplyTransactionType(supplyTransaction.getTransactionType())
                .quantity(supplyTransaction.getQuantity())
                .numberType(supplyTransaction.getNumberType())
                .note(supplyTransaction.getNote())
                .changeByUser(supplyTransaction.getApartmentUser().getUser().getFullName())
                .build();

        if(maintenance != null) {
            supplyTransactionSummary.setMaintenanceId(maintenance.getId());
            supplyTransactionSummary.setMaintenanceNumber(maintenance.getTicketNumber());
        }
        return supplyTransactionSummary;
    }
}
