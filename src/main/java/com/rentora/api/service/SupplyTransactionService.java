package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.mapper.SupplyTransactionMapper;
import com.rentora.api.model.dto.Supply.Request.UpdateSupplyRequestDto;
import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.repository.SupplyTransactionRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.SupplyTransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupplyTransactionService {
    private final SupplyTransactionMapper supplyTransactionMapper;

    private final SupplyTransactionRepository supplyTransactionRepository;
    private final ApartmentUserRepository apartmentUserRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository  apartmentRepository;

    public Page<SupplyTransactionSummaryResponseDto> getSupplyTransactions(UUID apartmentId, String supplyName,
                                                                           SupplyTransaction.SupplyTransactionType category, Pageable pageable) {
        Specification<SupplyTransaction> supplyTransactionSpecification = SupplyTransactionSpecification.hasApartmentId(apartmentId)
                .and(SupplyTransactionSpecification.hasSupplyName(supplyName));

        Page<SupplyTransaction> supplyTransactions = supplyTransactionRepository.findAll(supplyTransactionSpecification,pageable);

        return supplyTransactions.map(supplyTransactionMapper::supplyTransactionSummaryResponseDto);
    }

    // Create a transaction when maintenance use supply
    public void createMaintenanceUseSupplyTransaction(MaintenanceSupply maintenanceSupply,UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Apartment apartment = maintenanceSupply.getSupply().getApartment();

        ApartmentUser apartmentUser = apartmentUserRepository.findByApartmentAndUser(apartment, user)
                .orElseThrow(() -> new BadRequestException("Apartment User not found"));

        int supplyUsage = maintenanceSupply.getQuantityUsed();
        String maintenanceTitle = maintenanceSupply.getMaintenance().getTitle();
        String supplyName = maintenanceSupply.getSupply().getName();

        SupplyTransaction supplyTransaction = new SupplyTransaction();
        supplyTransaction.setApartmentUser(apartmentUser);
        supplyTransaction.setMaintenance(maintenanceSupply.getMaintenance());
        supplyTransaction.setSupply(maintenanceSupply.getSupply());
        supplyTransaction.setQuantity(supplyUsage);
        supplyTransaction.setNumberType(SupplyTransaction.SupplyTransactionNumberType.negative);
        supplyTransaction.setTransactionType(SupplyTransaction.SupplyTransactionType.use);
        supplyTransaction.setNote(maintenanceTitle + " use " + supplyUsage + " " + maintenanceSupply.getSupply().getUnit()
        +" " + "of " + supplyName);

        supplyTransactionRepository.save(supplyTransaction);
    }
    // Create a transaction when supply quantity changes (without maintenance)
    public void createSupplyUpdateTransaction(Supply supply, UpdateSupplyRequestDto request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Apartment apartment = supply.getApartment();

        ApartmentUser apartmentUser = apartmentUserRepository.findByApartmentAndUser(apartment, user)
                .orElseThrow(() -> new BadRequestException("Apartment User not found"));

        int newQuantity = request.getStockQuantity();
        int oldQuantity = supply.getStockQuantity();
        int difference = newQuantity - oldQuantity; // positive means added, negative means removed

        if (difference == 0) {
            // No change in quantity → no need to log transaction
            return;
        }

        SupplyTransaction transaction = getSupplyTransaction(supply, apartmentUser, difference);

        supplyTransactionRepository.save(transaction);
    }

    private SupplyTransaction getSupplyTransaction(Supply supply, ApartmentUser apartmentUser, int difference) {
        SupplyTransaction transaction = new SupplyTransaction();
        transaction.setSupply(supply);
        transaction.setApartmentUser(apartmentUser);
        transaction.setQuantity(Math.abs(difference));

        if (difference > 0) {
            transaction.setTransactionType(SupplyTransaction.SupplyTransactionType.purchase);
            transaction.setNumberType(SupplyTransaction.SupplyTransactionNumberType.positive);
            transaction.setNote(supply.getName() + " Added to stock " + difference + " units to stock");
        } else {
            transaction.setTransactionType(SupplyTransaction.SupplyTransactionType.adjustment);
            transaction.setNumberType(SupplyTransaction.SupplyTransactionNumberType.negative);
            transaction.setNote(supply.getName() + " stock removed " + Math.abs(difference) + " units from stock");
        }
        return transaction;
    }

    //create custom transaction
    public void createCustomMaintenanceSupplyTransaction(SupplyTransaction.SupplyTransactionType supplyTransactionType, User user
    ,Maintenance maintenance , Supply supply , Integer usageValue,String note,UUID apartmentId,
                                                         SupplyTransaction.SupplyTransactionNumberType numberType) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(()-> new BadRequestException("Apartment User not found"));

        ApartmentUser apartmentUser = apartmentUserRepository.findByApartmentAndUser(apartment, user)
                .orElseThrow(() -> new BadRequestException("Apartment User not found"));
        SupplyTransaction supplyTransaction = new SupplyTransaction();
        supplyTransaction.setSupply(supply);
        supplyTransaction.setMaintenance(maintenance);
        supplyTransaction.setTransactionType(supplyTransactionType);
        supplyTransaction.setNote(note);
        supplyTransaction.setQuantity(usageValue);
        supplyTransaction.setApartmentUser(apartmentUser);
        supplyTransaction.setNumberType(numberType);
        supplyTransactionRepository.save(supplyTransaction);
    }
}
