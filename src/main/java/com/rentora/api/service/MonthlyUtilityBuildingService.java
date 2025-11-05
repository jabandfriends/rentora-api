package com.rentora.api.service;

import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingDetailDTO;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingUsageSummary;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.MonthlyUtilityBuildingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityBuildingService {

    private final MonthlyUtilityBuildingRepository monthlyUtilityBuildingRepository;

    public List<MonthlyUtilityBuildingDetailDTO> getApartmentUtilitySummaryByBuilding(Apartment apartment) {

        List<UnitUtilities> entities = monthlyUtilityBuildingRepository
                .findAllByUnit_Floor_Building_Apartment(apartment);

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(entities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> entity.getUnit().getFloor().getBuilding(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        this::processBuildingUtilities
                                )
                        )
                )
                .values());
    }

    private MonthlyUtilityBuildingDetailDTO processBuildingUtilities(
            List<UnitUtilities> buildingUtilities) {

        Building building = buildingUtilities.getFirst().getUnit().getFloor().getBuilding();

        Map<String, Map<LocalDate, BigDecimal>> aggregatedData =
                aggregateUtilitiesByMonthAndType(buildingUtilities);

        return toMonthlyUtilityDetailDTO(building, aggregatedData);
    }

    private Map<String, Map<LocalDate, BigDecimal>> aggregateUtilitiesByMonthAndType(
            List<UnitUtilities> unitUtilities) {

        return unitUtilities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> entity.getUtility().getUtilityName(),
                                Collectors.groupingBy(
                                        entity -> entity.getUsageMonth().withDayOfMonth(1),
                                        Collectors.reducing(BigDecimal.ZERO, UnitUtilities::getUsageAmount, BigDecimal::add)
                                )
                        )
                );
    }

    private MonthlyUtilityBuildingDetailDTO toMonthlyUtilityDetailDTO(
            Building building,
            Map<String, Map<LocalDate, BigDecimal>> aggregatedData) {

        String buildingName = building.getName();

        Map<String, List<MonthlyUtilityBuildingUsageSummary>> utilityGroupName = aggregatedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                utilityGroup -> utilityGroup.getValue().entrySet().stream()
                                        .map(monthEntry ->
                                                toMonthlyUtilityUsageSummaryDTO(monthEntry.getKey(), monthEntry.getValue())
                                        )
                                        .collect(Collectors.toList())
                        )
                );

        MonthlyUtilityBuildingDetailDTO buildingDetail = new MonthlyUtilityBuildingDetailDTO();
        buildingDetail.setBuildingID(building.getId());
        buildingDetail.setBuildingName(buildingName);
        buildingDetail.setUtilityGroupName(utilityGroupName);

        return buildingDetail;
    }

    public MonthlyUtilityBuildingUsageSummary toMonthlyUtilityUsageSummaryDTO(
            LocalDate monthKey,
            BigDecimal totalUsage) {

        MonthlyUtilityBuildingUsageSummary monthlyUsage = new MonthlyUtilityBuildingUsageSummary();

        String monthName = monthKey.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        monthlyUsage.setMonth(monthName);
        monthlyUsage.setTotalUsageAmount(totalUsage);

        return monthlyUsage;
    }
}