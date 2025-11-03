package com.rentora.api.service;

import com.rentora.api.model.dto.MonthlyUtilityUnit.Response.MonthlyUtilityUnitDetailDTO;
import com.rentora.api.model.dto.MonthlyUtilityUnit.Response.MonthlyUtilityUsageSummaryDTO;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.MonthlyUtilityUnitRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityUnitService {

    private final MonthlyUtilityUnitRepository monthlyUtilityUnitRepository;
    private final UnitUtilityRepository unitUtilityRepository;

    public MonthlyUtilityUnitDetailDTO getMonthlyUtilitySummary(UUID unitId) {

        List<UnitUtilities> entities = monthlyUtilityUnitRepository.findAllByUnitId(unitId);

        if (entities.isEmpty()) {
            return null;
        }
        return toMonthlyUtilityDetailDTO(entities);
    }


    public MonthlyUtilityUnitDetailDTO toMonthlyUtilityDetailDTO (List<UnitUtilities> unitUtilities) {

        UnitUtilities firstUnitInfo = unitUtilities.getFirst();

        UUID unitId = firstUnitInfo.getUnit().getId();
        String unitName = firstUnitInfo.getUnit().getUnitName();
        Integer floorNumber = firstUnitInfo.getUnit().getFloor().getFloorNumber();
        String buildingName = firstUnitInfo.getUnit().getFloor().getBuilding().getName();

        MonthlyUtilityUnitDetailDTO unitUtility = new MonthlyUtilityUnitDetailDTO();

        unitUtility.setUnitId(unitId);
        unitUtility.setUnitName(unitName);
        unitUtility.setFloorNumber(floorNumber);
        unitUtility.setBuildingName(buildingName);


        Map<String, List<MonthlyUtilityUsageSummaryDTO>> utilityGroupName = unitUtilities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> entity.getUtility().getUtilityName(),
                                Collectors.mapping(this::toMonthlyUtilityUsageSummaryDTO, Collectors.toList())
                        )
                );

        unitUtility.setUtilityGroupName(utilityGroupName);


        return unitUtility;
    }


    public MonthlyUtilityUsageSummaryDTO toMonthlyUtilityUsageSummaryDTO(UnitUtilities unitUtilities) {
        MonthlyUtilityUsageSummaryDTO monthlyUsage = new MonthlyUtilityUsageSummaryDTO();

        if (unitUtilities.getUsageMonth() != null) {
            String monthName = unitUtilities.getUsageMonth().getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthlyUsage.setMonth(monthName);
        }

        monthlyUsage.setUsageAmount(unitUtilities.getUsageAmount());

        return monthlyUsage;
    }


}
