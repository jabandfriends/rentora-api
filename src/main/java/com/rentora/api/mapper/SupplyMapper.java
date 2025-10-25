package com.rentora.api.mapper;

import com.rentora.api.model.dto.Supply.Request.CreateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Request.UpdateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Response.SupplyMetaDataDto;
import com.rentora.api.model.dto.Supply.Response.SupplySummaryResponseDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Supply;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SupplyMapper {

    public Supply toCreateSupply(Apartment apartment, CreateSupplyRequestDto request) {
        Supply supply = new Supply();
        supply.setName(request.getName());
        supply.setCategory(request.getCategory());
        supply.setApartment(apartment);
        supply.setDescription(request.getDescription());
        supply.setUnit(request.getUnit());
        supply.setStockQuantity(request.getStockQuantity());
        supply.setMinStock(request.getMinStock());
        supply.setCostPerUnit(request.getCostPerUnit());

        return supply;
    }

    public void toUpdateSupply(Supply supply, UpdateSupplyRequestDto request) {
        if(request.getName() != null && !request.getName().isEmpty()) supply.setName(request.getName());
        if(request.getCategory() != null) supply.setCategory(request.getCategory());
        if(request.getDescription() != null && !request.getDescription().isEmpty()) supply.setDescription(request.getDescription());
        if(request.getUnit() != null && !request.getUnit().isEmpty()) supply.setUnit(request.getUnit());
        if(request.getStockQuantity() != null) supply.setStockQuantity(request.getStockQuantity());
        if(request.getMinStock() != null) supply.setMinStock(request.getMinStock());
        if(request.getCostPerUnit() != null) supply.setCostPerUnit(request.getCostPerUnit());

    }

    public SupplySummaryResponseDto toSupplySummaryResponseDto(Supply supply) {
        if (supply == null) return null;
        BigDecimal unitPrice = supply.getCostPerUnit() != null ? supply.getCostPerUnit() : BigDecimal.ZERO;
        int quantity = supply.getStockQuantity() != null ? supply.getStockQuantity() : 0;
        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return SupplySummaryResponseDto.builder()
                .supplyId(supply.getId())
                .supplyName(supply.getName())
                .supplyQuantity(supply.getStockQuantity())
                .supplyMinStock(supply.getMinStock())
                .supplyUnit(supply.getUnit())
                .supplyUnitPrice(supply.getCostPerUnit())
                .supplyCategory(supply.getCategory())
                .supplyStockStatus(supply.getSupplyStockStatus())
                .supplyTotalCost(totalCost)
                .build();
    }

    public SupplyMetaDataDto toSupplyMetaDataDto(long totalSupplies,long totalLowStockSupplies,BigDecimal totalCostSupplies) {
        return SupplyMetaDataDto.builder()
                .totalSupplies(totalSupplies)
                .totalLowStockSupplies(totalLowStockSupplies)
                .totalCostSupplies(totalCostSupplies)
                .build();
    }
}
