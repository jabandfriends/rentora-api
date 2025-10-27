package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.MaintenanceSupply;
import com.rentora.api.model.entity.Supply;
import com.rentora.api.model.entity.SupplyTransaction;
import com.rentora.api.repository.MaintenanceSupplyRepository;
import com.rentora.api.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MaintenanceSupplyService {

    private final MaintenanceSupplyRepository maintenanceSupplyRepository;
    private final SupplyRepository supplyRepository;

    private final SupplyTransactionService supplyTransactionService;

    //maintenance use supply
    public void maintenanceUseSupply(Maintenance maintenance, UUID supplyId,Integer quantity,UUID userId) {
        if(quantity<=0) return;

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
        maintenanceSupplyRepository.save(maintenanceSupply);

        //add supply transaction
        supplyTransactionService.createMaintenanceUseSupplyTransaction(maintenanceSupply,userId);
    }

}
