package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.UnitUtility.Request.CreateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Request.UnitUtility;
import com.rentora.api.model.dto.UnitUtility.Request.UpdateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Response.AvailableMonthsDto;
import com.rentora.api.model.dto.UnitUtility.Response.AvailableYearsDto;
import com.rentora.api.model.dto.UnitUtility.Response.UnitWithUtilityResponseDto;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.repository.UtilityRepository;
import com.rentora.api.specifications.UnitSpecification;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitUtilityService {

    private final UnitUtilityRepository unitUtilityRepository;
    private final UtilityRepository utilityRepository;
    private final UnitRepository unitRepository;
    private final ContractRepository contractRepository;


    public List<UnitWithUtilityResponseDto> getAllUnitWithUtility(UUID apartmentId,String buildingName) {
        Specification<Unit>  unitSpecification = UnitSpecification.hasApartmentId(apartmentId)
                .and(UnitSpecification.hasBuildingName(buildingName));
        List<Unit> allUnits = unitRepository.findAll(unitSpecification);

        List<UnitWithUtilityResponseDto> unitResult = new ArrayList<>();
        for (Unit unit : allUnits){
            //try to find active contract
            Optional<Contract> activeContractOpt = contractRepository.findActiveContractByUnitId(unit.getId());
            Contract activeContract = activeContractOpt.orElse(null);

            //Utility
            UnitUtilities lastWater = findLastReading(unit,"water");
            UnitUtilities lastElectric = findLastReading(unit,"electric");

            BigDecimal waterStart = null;
            BigDecimal electricStart = null;
            if(lastWater != null){
                waterStart = lastWater.getMeterEnd();
            }else if(activeContract != null){
                waterStart = activeContract.getWaterMeterStartReading();
            }

            if (lastElectric != null) {
                electricStart = lastElectric.getMeterEnd();
            } else if (activeContract != null) {
                electricStart = activeContract.getElectricityMeterStartReading();
            }

            unitResult.add(UnitWithUtilityResponseDto.builder().unitId(unit.getId())
                    .unitStatus(unit.getStatus()).unitName(unit.getUnitName()).buildingName(unit.getFloor().getBuilding().getName())
                    .electricMeterStart(electricStart).waterMeterStart(waterStart).build());
        }
        return unitResult;
    }

    public UnitUtilities findLastReading(Unit unit , String utilityName){
        // ===== Utility =====
        Specification<Utility> utilitySpecification = UtilitySpecification
                .hasApartmentId(unit.getFloor().getBuilding().getApartment().getId())
                .and(UtilitySpecification.hasUtilityName(utilityName));
        Utility utility = utilityRepository.findOne(utilitySpecification)
                .orElseThrow(() -> new ResourceNotFoundException("Water utility not found"));
        // ===== Find Last utility Reading =====
        Specification<UnitUtilities> unitUtilitySpec = UnitUtilitySpecification.hasUnitId(unit.getId())
                .and(UnitUtilitySpecification.hasUtilityId(utility.getId()));
        List<UnitUtilities> unitUtilities = unitUtilityRepository.findAll(unitUtilitySpec);
        unitUtilities.sort(Comparator.comparing(UnitUtilities::getUsageMonth).reversed());

        return unitUtilities.isEmpty() ? null : unitUtilities.getFirst();
    }

    public void updateUnitUtility(UUID apartmentId,@RequestBody @Valid UpdateUnitUtilityRequestDto request){
        List<UnitUtilities> unitUtilitiesResult = new ArrayList<>();
        //water utility
        Specification<UnitUtilities> waterSpec = UnitUtilitySpecification.hasId(request.getWaterUnitUtilityId())
                .and(UnitUtilitySpecification.hasApartmentId(apartmentId));
        UnitUtilities waterUtility = unitUtilityRepository.findOne(waterSpec)
                .orElseThrow(()-> new ResourceNotFoundException("Water utility not found"));

        if(request.getWaterEnd() != null) waterUtility.setMeterEnd(request.getWaterEnd());
        if(request.getWaterStart() != null) waterUtility.setMeterStart(request.getWaterStart());
        //total Usage
        BigDecimal waterUsage =  waterUtility.getMeterEnd().subtract(waterUtility.getMeterStart());
        waterUtility.setUsageAmount(waterUsage);
        //totalCost
        BigDecimal waterTotalCost;
        BigDecimal waterPricePerUnit = waterUtility.getUtility().getUnitPrice();
        BigDecimal waterFixedPrice = waterUtility.getUtility().getFixedPrice();
        Utility.UtilityType waterCostType = waterUtility.getUtility().getUtilityType();
        if(waterCostType == Utility.UtilityType.meter) {
            waterTotalCost= waterUsage.multiply(waterPricePerUnit);
            waterUtility.setCalculatedCost(waterTotalCost);
        }
        if(waterCostType == Utility.UtilityType.fixed) {
            waterTotalCost = waterFixedPrice;
            waterUtility.setCalculatedCost(waterTotalCost);
        }

        unitUtilitiesResult.add(waterUtility);

        //electric utility
        Specification<UnitUtilities> electricSpec = UnitUtilitySpecification.hasId(request.getElectricUnitUtilityId())
                .and(UnitUtilitySpecification.hasApartmentId(apartmentId));
        UnitUtilities electricUtility = unitUtilityRepository.findOne(electricSpec)
                .orElseThrow(()-> new ResourceNotFoundException("Electric utility not found"));

        if(request.getElectricEnd() != null) electricUtility.setMeterEnd(request.getElectricEnd());
        if(request.getElectricStart() != null) electricUtility.setMeterStart(request.getElectricStart());

        //total Usage
        BigDecimal electricUsage =  electricUtility.getMeterEnd().subtract(electricUtility.getMeterStart());
        electricUtility.setUsageAmount(electricUsage);
        //totalCost
        BigDecimal electricTotalCost;
        BigDecimal electricPricePerUnit = electricUtility.getUtility().getUnitPrice();
        BigDecimal electricFixedPrice = electricUtility.getUtility().getFixedPrice();
        Utility.UtilityType electricCostType = electricUtility.getUtility().getUtilityType();
        if(electricCostType == Utility.UtilityType.meter) {
            electricTotalCost= electricUsage.multiply(electricPricePerUnit);
            electricUtility.setCalculatedCost(electricTotalCost);
        }
        if(electricCostType == Utility.UtilityType.fixed) {
            electricTotalCost = electricFixedPrice;
            electricUtility.setCalculatedCost(electricTotalCost);
        }

        unitUtilitiesResult.add(electricUtility);
        unitUtilityRepository.saveAll(unitUtilitiesResult);

        log.info("Update utility successfully for {}",electricUtility.getUnit().getUnitName());
    }
    public void createUnitUtility(UUID apartmentId, @RequestBody @Valid CreateUnitUtilityRequestDto requestDto) {
        LocalDate usageMonth = LocalDate.now().withMonth(requestDto.getReadingMonth())
                .withYear(requestDto.getReadingYear()).withDayOfMonth(1);

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
            UnitUtilities lastWaterReading = findLastReading(unit,"water");

            // ===== Find Last Electric Reading =====
            UnitUtilities lastElectricReading = findLastReading(unit,"electric");

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

    // Get available years
    public AvailableYearsDto getAvailableYears(UUID apartmentId) {
        // All usageMonths recorded for this apartment
        List<LocalDate> usageMonths = unitUtilityRepository.findAllUsageMonthsByApartment(apartmentId);

        int currentYear = LocalDate.now().getYear();

        // Count how many months are used per year
        Map<Integer, Long> monthsCountByYear = usageMonths.stream()
                .collect(Collectors.groupingBy(LocalDate::getYear, Collectors.counting()));

        // Include years that are not fully used (less than 12 months recorded)
        List<Integer> availableYears = IntStream.rangeClosed(currentYear, currentYear + 5)
                .boxed()
                .filter(y -> monthsCountByYear.getOrDefault(y, 0L) < 12)
                .collect(Collectors.toList());

        return AvailableYearsDto.builder().years(availableYears).build();
    }

    // Get available months for a given year
    public AvailableMonthsDto getAvailableMonths(UUID apartmentId, String buildingName, int year) {
        List<LocalDate> usageMonths = unitUtilityRepository.findAllUsageMonthsByApartmentAndBuilding(apartmentId, buildingName);

        Set<Integer> usedMonths = usageMonths.stream()
                .filter(d -> d.getYear() == year)
                .map(LocalDate::getMonthValue)
                .collect(Collectors.toSet());

        List<Integer> availableMonths = IntStream.rangeClosed(1, 12)
                .filter(m -> !usedMonths.contains(m))
                .boxed()
                .collect(Collectors.toList());

        return AvailableMonthsDto.builder()
                .months(availableMonths)
                .year(year)
                .build();
    }

}
