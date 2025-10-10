package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.UnitUtility.Request.CreateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Request.UnitUtility;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.repository.UtilityRepository;
import com.rentora.api.specifications.UnitUtilitySpecification;
import com.rentora.api.specifications.UtilitySpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitUtilityService {

    private final UnitUtilityRepository unitUtilityRepository;
    private final UtilityRepository utilityRepository;
    private final UnitRepository unitRepository;
    private final ContractRepository contractRepository;

    public void createUnitUtility(UUID apartmentId, @RequestBody @Valid CreateUnitUtilityRequestDto requestDto) {
        LocalDate usageMonth = requestDto.getMeterDate().withDayOfMonth(1);

        for (UnitUtility room : requestDto.getRooms()) {
            Unit unit = unitRepository.findById(room.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

            // ===== Water Utility =====
            Specification<Utility> waterUtilitySpecification = UtilitySpecification
                    .hasApartmentId(unit.getFloor().getBuilding().getApartment().getId())
                    .and(UtilitySpecification.hasUtilityName("water"));
            Utility waterUtility = utilityRepository.findOne(waterUtilitySpecification)
                    .orElseThrow(() -> new ResourceNotFoundException("Water utility not found"));

            // ===== Electric Utility =====
            Specification<Utility> electricUtilitySpecification = UtilitySpecification
                    .hasApartmentId(unit.getFloor().getBuilding().getApartment().getId())
                    .and(UtilitySpecification.hasUtilityName("electric"));
            Utility electricUtility = utilityRepository.findOne(electricUtilitySpecification)
                    .orElseThrow(() -> new ResourceNotFoundException("Electric utility not found"));

            // ===== Find Last Water Reading =====
            Specification<UnitUtilities> waterUtilitySpec = UnitUtilitySpecification.hasUnitId(unit.getId())
                    .and(UnitUtilitySpecification.hasUtilityId(waterUtility.getId()));
            List<UnitUtilities> unitWaterUtilities = unitUtilityRepository.findAll(waterUtilitySpec);
            unitWaterUtilities.sort(Comparator.comparing(UnitUtilities::getUsageMonth).reversed());
            UnitUtilities lastWaterReading = unitWaterUtilities.isEmpty() ? null : unitWaterUtilities.getFirst();

            // ===== Find Last Electric Reading =====
            Specification<UnitUtilities> electricUtilitySpec = UnitUtilitySpecification.hasUnitId(unit.getId())
                    .and(UnitUtilitySpecification.hasUtilityId(electricUtility.getId()));
            List<UnitUtilities> unitElectricUtilities = unitUtilityRepository.findAll(electricUtilitySpec);
            unitElectricUtilities.sort(Comparator.comparing(UnitUtilities::getUsageMonth).reversed());
            UnitUtilities lastElectricReading = unitElectricUtilities.isEmpty() ? null : unitElectricUtilities.getFirst();

            // ===== Prepare new water reading =====
            BigDecimal waterEnd = BigDecimal.valueOf(room.getWaterEnd());
            BigDecimal waterStart;
            if (lastWaterReading != null) {
                waterStart = lastWaterReading.getMeterEnd();
            } else {
                Optional<Contract> contractOpt = contractRepository.findActiveContractByUnitId(unit.getId());
                waterStart = contractOpt.map(Contract::getWaterMeterStartReading)
                        .orElse(BigDecimal.valueOf(room.getWaterStart()));
            }

            if (waterEnd.compareTo(waterStart) < 0) {
                throw new IllegalArgumentException("Water end meter can't be less than water start meter");
            }

            BigDecimal waterUsage = waterEnd.subtract(waterStart);
            UnitUtilities newWaterUtility = new UnitUtilities();
            newWaterUtility.setUnit(unit);
            newWaterUtility.setReadingDate(LocalDate.now());
            newWaterUtility.setUtility(waterUtility);
            newWaterUtility.setMeterStart(waterStart);
            newWaterUtility.setMeterEnd(waterEnd);
            newWaterUtility.setUsageAmount(waterUsage);
            newWaterUtility.setUsageMonth(usageMonth);
            newWaterUtility.setNotes("Auto-created water reading");
            BigDecimal totalWaterCost;
            Utility.UtilityType waterCalculateType = waterUtility.getUtilityType();
            if(waterCalculateType.equals(Utility.UtilityType.meter)){
                totalWaterCost = waterUsage.multiply(electricUtility.getUnitPrice());
                newWaterUtility.setCalculatedCost(totalWaterCost);
            }
            if(waterCalculateType.equals(Utility.UtilityType.fixed)){
                totalWaterCost = waterUtility.getFixedPrice();
                newWaterUtility.setCalculatedCost(totalWaterCost);
            }
            unitUtilityRepository.save(newWaterUtility);

            // ===== Prepare new electric reading =====
            BigDecimal electricEnd = BigDecimal.valueOf(room.getElectricEnd());
            BigDecimal electricStart;
            if (lastElectricReading != null) {
                electricStart = lastElectricReading.getMeterEnd();
            } else {
                Optional<Contract> contractOpt = contractRepository.findActiveContractByUnitId(unit.getId());
                electricStart = contractOpt.map(Contract::getElectricityMeterStartReading)
                        .orElse(BigDecimal.valueOf(room.getElectricStart()));
            }

            if (electricEnd.compareTo(electricStart) < 0) {
                throw new IllegalArgumentException("Electric end meter can't be less than electric start meter");
            }

            BigDecimal electricUsage = electricEnd.subtract(electricStart);
            UnitUtilities newElectricUtility = new UnitUtilities();
            newElectricUtility.setUnit(unit);
            newElectricUtility.setReadingDate(LocalDate.now());
            newElectricUtility.setUtility(electricUtility);
            newElectricUtility.setMeterStart(electricStart);
            newElectricUtility.setMeterEnd(electricEnd);
            newElectricUtility.setUsageAmount(electricUsage);
            newElectricUtility.setUsageMonth(usageMonth);
            newElectricUtility.setNotes("Auto-created electric reading");
            //set calculated cost
            BigDecimal totalElectricCost;
            Utility.UtilityType electricCalculatedType = electricUtility.getUtilityType();
            if(electricCalculatedType.equals(Utility.UtilityType.meter)){
                totalElectricCost = electricUsage.multiply(electricUtility.getUnitPrice());
                newElectricUtility.setCalculatedCost(totalElectricCost);
            }
            if(electricCalculatedType.equals(Utility.UtilityType.fixed)){
                totalElectricCost = electricUtility.getFixedPrice();
                newElectricUtility.setCalculatedCost(totalElectricCost);
            }


            unitUtilityRepository.save(newElectricUtility);
        }
    }

}
