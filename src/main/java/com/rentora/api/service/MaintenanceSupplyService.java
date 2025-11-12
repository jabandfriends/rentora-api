package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.MaintenanceSupplyRepository;
import com.rentora.api.repository.SupplyRepository;
import com.rentora.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MaintenanceSupplyService {

    private final MaintenanceSupplyRepository maintenanceSupplyRepository;
    private final SupplyRepository supplyRepository;
    private final UserRepository userRepository;

    private final SupplyTransactionService supplyTransactionService;

    //maintenance use supply
    public MaintenanceSupply maintenanceUseSupply(Maintenance maintenance, UUID supplyId, Integer quantity, UUID userId) {
        if(quantity<=0) return null;

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(()-> new ResourceNotFoundException("Supply not found"));
        //check is supply delete
        if(supply.getIsDeleted()) throw new BadRequestException(supply.getName()+" is already deleted");
        Integer supplyQuantity = supply.getStockQuantity();

        //update supply stock
        int updatedQuantity = supplyQuantity-quantity;
        if(updatedQuantity < 0) throw new BadRequestException(supply.getName() +" is not enough");
        supply.setStockQuantity(updatedQuantity);

        MaintenanceSupply maintenanceSupply = new MaintenanceSupply();
        maintenanceSupply.setMaintenance(maintenance);
        maintenanceSupply.setSupply(supply);
        maintenanceSupply.setQuantityUsed(quantity);

        BigDecimal totalCost = supply.getCostPerUnit().multiply(BigDecimal.valueOf(quantity));
        maintenanceSupply.setCost(totalCost);

        //add supply transaction
        return maintenanceSupply;
    }

    public void maintenanceUpdateSupply(UUID apartmentId,UUID maintenanceSupplyId,UUID supplyId,Integer newUsageSupply, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        Supply supply = supplyRepository.findById(supplyId).orElseThrow(()-> new ResourceNotFoundException("Supply not found"));
        if(supply.getIsDeleted()) throw new BadRequestException(supply.getName()+" is already deleted");
        MaintenanceSupply maintenanceSupply = maintenanceSupplyRepository.findById(maintenanceSupplyId)
                .orElseThrow(()-> new ResourceNotFoundException("Maintenance Supply not found"));

        //if new usage = before usage = no update
        if(Objects.equals(newUsageSupply, maintenanceSupply.getQuantityUsed())) return;

        Integer supplyStockQuantity = supply.getStockQuantity();
        Integer maintenanceUsageQuantity = maintenanceSupply.getQuantityUsed();


        if(newUsageSupply.equals(0)) {
            Integer total = supplyStockQuantity+maintenanceUsageQuantity;
            supply.setStockQuantity(total);
            supplyTransactionService.createCustomMaintenanceSupplyTransaction(SupplyTransaction.SupplyTransactionType.adjustment,
                    user,maintenanceSupply.getMaintenance(),supply,maintenanceUsageQuantity,"Removed maintenance supply usage stock increase",apartmentId,
                    SupplyTransaction.SupplyTransactionNumberType.positive);
            maintenanceSupplyRepository.delete(maintenanceSupply);
            return;
        }
        //maintenance new usage > before usage
        if (newUsageSupply > maintenanceUsageQuantity) {

            //remove stock
            Integer totalRemoveStock =  newUsageSupply - maintenanceUsageQuantity;

            //check stock is enough
            if(supplyStockQuantity < totalRemoveStock) throw new BadRequestException(supply.getName()+" is not enough");

            //update stock
            Integer newStockQuantity = supplyStockQuantity - totalRemoveStock;
            supply.setStockQuantity(newStockQuantity);
            maintenanceSupply.setQuantityUsed(newUsageSupply);

            //add transaction decrease more stock
            supplyTransactionService.createCustomMaintenanceSupplyTransaction(SupplyTransaction.SupplyTransactionType.adjustment,
                    user,maintenanceSupply.getMaintenance(),supply,totalRemoveStock,"Stock Decreased ",apartmentId
                            , SupplyTransaction.SupplyTransactionNumberType.negative);
        }else{
            //maintenance new usage < before

            //increase stock
            Integer totalStockUsage =  maintenanceUsageQuantity - newUsageSupply;
            //update
            Integer newUsageStock = supplyStockQuantity + totalStockUsage;
            //total update stock
            Integer totalUpdate = newUsageStock - supplyStockQuantity;

            supply.setStockQuantity(newUsageStock);
            maintenanceSupply.setQuantityUsed(newUsageSupply);

            //add transaction back adjust but stock is increase
            supplyTransactionService.createCustomMaintenanceSupplyTransaction(SupplyTransaction.SupplyTransactionType.adjustment,
                    user,maintenanceSupply.getMaintenance(),supply,totalUpdate,"Stock Increased usage",apartmentId,
                    SupplyTransaction.SupplyTransactionNumberType.positive);
        }

        maintenanceSupplyRepository.save(maintenanceSupply);
        supplyRepository.save(supply);

    }

    public void removeMaintenanceList(UUID apartmentId, List<MaintenanceSupply> maintenanceSupplyList, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        for (MaintenanceSupply maintenanceSupply : maintenanceSupplyList) {
            Supply supply = maintenanceSupply.getSupply();

            // Return used stock back to supply
            Integer returnedStock = maintenanceSupply.getQuantityUsed();
            supply.setStockQuantity(supply.getStockQuantity() + returnedStock);

            // Log transaction for stock return
            supplyTransactionService.createCustomMaintenanceSupplyTransaction(
                    SupplyTransaction.SupplyTransactionType.adjustment,
                    user,
                    maintenanceSupply.getMaintenance(),
                    supply,
                    returnedStock, // positive value for stock increase
                    "Deleted maintenance supply usage",
                    apartmentId,
                    SupplyTransaction.SupplyTransactionNumberType.positive
            );

            // Save updated supply
            supplyRepository.save(supply);
        }

        // Delete all maintenance supply records
        maintenanceSupplyRepository.deleteAll(maintenanceSupplyList);
    }

}
