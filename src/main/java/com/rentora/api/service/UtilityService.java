package com.rentora.api.service;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UtilityService {
    private final UnitUtilityRepository unitUtilityRepository;
    private final ContractRepository contractRepository;

    public List<ReportUtiltyDetailDto> getAllUnitsUtility(Pageable pageable){

        Page<UnitUtilities> unitUtilitiesPage = unitUtilityRepository.findAll(pageable);
        List<UnitUtilities> unitUtilities = unitUtilitiesPage.getContent();

        // Group utilities by unit
        Map<UUID, List<UnitUtilities>> utilitiesByUnit = unitUtilities.stream()
                .collect(Collectors.groupingBy(u -> u.getUnit().getId()));

        List<ReportUtiltyDetailDto> result = new ArrayList<>();

        for (UUID unitId : utilitiesByUnit.keySet()) {
            List<UnitUtilities> utilities = utilitiesByUnit.get(unitId);

            // water and electric
            UnitUtilities water = utilities.stream()
                    .filter(u -> u.getUtility().getUtilityName().equals("water"))
                    .findFirst().orElse(null);

            UnitUtilities electric = utilities.stream()
                    .filter(u -> u.getUtility().getUtilityName().equals("electric"))
                    .findFirst().orElse(null);

            BigDecimal waterUsage = null;
            BigDecimal electricUsage = null;
            BigDecimal waterCost = BigDecimal.ZERO;
            BigDecimal electricCost = BigDecimal.ZERO;

            if(water == null && electric == null){
                throw new EntityNotFoundException("Units not found");
            }

            if (water.getUtility().getUtilityType() == Utility.UtilityType.meter) {
                waterUsage = water.getMeterEnd().subtract(water.getMeterStart());
                water.setUsageAmount(waterUsage);
                unitUtilityRepository.save(water);
                waterCost = waterUsage.multiply(water.getUtility().getUnitPrice());
            } else {
                waterCost = water.getUtility().getFixedPrice();
            }


            if (electric.getUtility().getUtilityType() == Utility.UtilityType.meter) {
                electricUsage = electric.getMeterEnd().subtract(electric.getMeterStart());
                electric.setUsageAmount(electricUsage);
                unitUtilityRepository.save(electric);
                electricCost = electricUsage.multiply(electric.getUtility().getUnitPrice());
            } else {
                electricCost = electric.getUtility().getFixedPrice();
            }


            electric.setCalculatedCost(electricCost);
            unitUtilityRepository.save(electric);
            //save coast

            water.setCalculatedCost(waterCost);
            unitUtilityRepository.save(water);
            Contract contract = contractRepository.findByUnitId(unitId)
                    .orElseThrow(() -> new EntityNotFoundException("unit not found"));

            BigDecimal totalPrice = waterCost.add(electricCost);

            ReportUtiltyDetailDto dto = new ReportUtiltyDetailDto();
            dto.setUnitName(contract.getUnit().getUnitName());
            dto.setTenantName(contract.getTenant().getFullName());
            dto.setTotalPrice(totalPrice);
            dto.setWaterUsage(waterUsage);
            dto.setElectricUsage(electricUsage);
            dto.setWaterCost(waterCost);
            dto.setElectricCost(electricCost);

            result.add(dto);
        }

        return result;
    }

    public ReportUtiltyDetailDto getUnitsUtility(UUID unitId, Pageable pageable){

        Page<UnitUtilities> unitUtilities = unitUtilityRepository.findByUnitId(unitId,pageable);
        List<UnitUtilities> utilities = unitUtilities.getContent();

        //category , meter_end , meter_start , unit_price
        UnitUtilities water = utilities.stream().filter(utilityItem-> utilityItem.getUtility().getUtilityName().equals("water")).toList().getFirst();
        //category ,meter_end , meter_start , unit_price
        UnitUtilities electric = utilities.stream().filter(utilityItem-> utilityItem.getUtility().getUtilityName().equals("electric")).toList().getFirst();

        BigDecimal waterUsage;
        BigDecimal electricUsage;
        BigDecimal waterCost;
        BigDecimal electricCost;

        Utility.UtilityType waterType = water.getUtility().getUtilityType();
        Utility.UtilityType electricType =  electric.getUtility().getUtilityType();

        //water
        if(waterType.equals(Utility.UtilityType.meter)){
            //unit amount
            waterUsage = water.getMeterEnd().subtract(water.getMeterStart());



            //water total price
            //saved to db
            waterCost = waterUsage.multiply(water.getUtility().getUnitPrice());
            water.setUsageAmount(waterUsage);


        }else{
            waterUsage = null;
            waterCost = water.getUtility().getFixedPrice();
        }

        //electric
        if(electricType.equals(Utility.UtilityType.meter)){
            //unit amount
            electricUsage = electric.getMeterEnd().subtract(electric.getMeterStart());

            //electric total price
            electricCost = electricUsage.multiply(electric.getUtility().getUnitPrice());

            //save to db
            electric.setUsageAmount(electricUsage);

        }else{
            electricUsage = null;
            electricCost = electric.getUtility().getFixedPrice();

        }
        electric.setCalculatedCost(electricCost);
        water.setCalculatedCost(waterCost);
        UnitUtilities electricUnit = unitUtilityRepository.save(electric);
        UnitUtilities waterUnit = unitUtilityRepository.save(water);

        //Contract
        Contract contract = contractRepository.findByUnitId(water.getUnit().getId()).orElseThrow(()->new EntityNotFoundException("unit not found"));

        //TenantName
        String tenantName = contract.getTenant().getFullName();

        //room
        String unitName = contract.getUnit().getUnitName();

        BigDecimal totalPrice = electricCost.add(waterCost);

        ReportUtiltyDetailDto response =  new ReportUtiltyDetailDto();
        response.setUnitName(unitName);
        response.setTenantName(tenantName);
        response.setTotalPrice(totalPrice);
        response.setElectricCost(electricCost);
        response.setWaterUsage(waterUsage);
        response.setElectricUsage(electricUsage);
        response.setWaterCost(waterCost);
        return response;

    }

    @Data
    public static class ReportUtiltyDetailDto{
        private String unitName;
        private String tenantName;
        private BigDecimal electricUsage;
        private BigDecimal electricCost;
        private BigDecimal waterUsage;
        private BigDecimal waterCost;
        private BigDecimal totalPrice;
    }
}
