package com.rentora.api.service;

import com.rentora.api.model.dto.ApartmentUtility.response.ApartmentUtilityMonthlyUsage;
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

import java.math.BigDecimal;
import java.time.Month;
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

    private static final String ELECTRIC = "electric";
    private static final String WATER = "water";

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


        Map<String, List<MonthlyUtilityUsageSummaryDTO>> monthlyBreakdown = unitUtilities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> entity.getUtility().getUtilityName(),
                                Collectors.mapping(this::toMonthlyUtilityUsageSummaryDTO, Collectors.toList())
                        )
                );

        Map<String, List<MonthlyUtilityUsageSummaryDTO>> finalBreakdown =
                fillMissingMonths(monthlyBreakdown);


        unitUtility.setUtilityGroupName(finalBreakdown);


        return  unitUtility;
    }


//    public MonthlyUtilityUsageSummaryDTO toMonthlyUtilityUsageSummaryDTO(UnitUtilities unitUtilities) {
//        MonthlyUtilityUsageSummaryDTO monthlyUsage = new MonthlyUtilityUsageSummaryDTO();
//
//        if (unitUtilities.getUsageMonth() != null) {
//            String monthName = unitUtilities.getUsageMonth().getMonth()
//                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
//            monthlyUsage.setMonth(monthName);
//        }
//
//        monthlyUsage.setUsageAmount(unitUtilities.getUsageAmount());
//
//        return monthlyUsage;
//    }


    public MonthlyUtilityUsageSummaryDTO toMonthlyUtilityUsageSummaryDTO(UnitUtilities unitUtilities) {
        String monthName = null;

        if (unitUtilities.getUsageMonth() != null) {
             monthName = unitUtilities.getUsageMonth().getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        }

        return MonthlyUtilityUsageSummaryDTO.builder().month(monthName).usageAmount(unitUtilities.getUsageAmount()).build();
    }

    private Map<String, List<MonthlyUtilityUsageSummaryDTO>> fillMissingMonths(
            Map<String, List<MonthlyUtilityUsageSummaryDTO>> currentBreakdown) {

        Map<String, List<MonthlyUtilityUsageSummaryDTO>> finalMap = new HashMap<>();

        finalMap.put(ELECTRIC, fillSingleUtilityMonths(currentBreakdown.getOrDefault(ELECTRIC, Collections.emptyList())));
        finalMap.put(WATER, fillSingleUtilityMonths(currentBreakdown.getOrDefault(WATER, Collections.emptyList())));

        return finalMap;
    }


    private List<MonthlyUtilityUsageSummaryDTO> fillSingleUtilityMonths(List<MonthlyUtilityUsageSummaryDTO> monthlyData) {

        Map<String, MonthlyUtilityUsageSummaryDTO> existingData = monthlyData.stream()
                .collect(Collectors.toMap(
                        MonthlyUtilityUsageSummaryDTO::getMonth,
                        data -> data
                ));

        List<MonthlyUtilityUsageSummaryDTO> fullYearData = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (existingData.containsKey(monthName)) {
                fullYearData.add(existingData.get(monthName));
            } else {
                fullYearData.add(MonthlyUtilityUsageSummaryDTO.builder()
                        .month(monthName)
                        .usageAmount(BigDecimal.ZERO)
                        .build());
            }
        }
        return fullYearData;

    }


}
