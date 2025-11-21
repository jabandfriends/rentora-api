package com.rentora.api.service;

import com.rentora.api.model.dto.MonthlyUtilityBuilding.Metadata.MonthlyUtilityBuildingMetadata;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingDetailDTO;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingUsageSummary;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.MonthlyUtilityBuildingRepository;
import com.rentora.api.specifications.MonthlyUtilityBuildingSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.rentora.api.specifications.MonthlyUtilityBuildingSpecification.hasApartment;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityBuildingService {

    private final MonthlyUtilityBuildingRepository monthlyUtilityBuildingRepository;
    private final BuildingRepository buildingRepository;

    public MonthlyUtilityBuildingMetadata getMonthlyUtilityBuildingMetadata (
            Apartment apartment,
            List<MonthlyUtilityBuildingDetailDTO> monthlyUtilityBuilding) {

        UUID apartmentId = apartment.getId();

        long totalBuildingCount = buildingRepository.countByApartmentId(apartmentId);



        return MonthlyUtilityBuildingMetadata.builder().totalUtilityBuildings(totalBuildingCount).build();
    }

    public Page<MonthlyUtilityBuildingDetailDTO> getApartmentUtilitySummaryByBuilding(
            Apartment apartment,
            UUID buildingId,
            Pageable pageable) {

        Page<Building> Buildings;

        Specification<Building> spec = MonthlyUtilityBuildingSpecification.hasApartment(apartment);


        if (buildingId != null) {
            spec = spec.and(MonthlyUtilityBuildingSpecification.hasBuildingId(buildingId));
        }

        Buildings = buildingRepository.findAll(spec, pageable);

        return Buildings.map(building ->
                this.toBuildingSummaryWithAggregation(building, apartment)
        );
    }
    private MonthlyUtilityBuildingDetailDTO toBuildingSummaryWithAggregation(
            Building building,
            Apartment apartment) {

        List<UnitUtilities> entities = monthlyUtilityBuildingRepository.findAllByUnit_Floor_Building(building);

        if (entities.isEmpty()) {
            return createEmptyDTO(building);
        }

        Map<String, Map<LocalDate, BigDecimal>> aggregatedData =
                aggregateUtilitiesByMonthAndType(entities);

        return toMonthlyUtilityDetailDTO(building, aggregatedData);
    }

    private MonthlyUtilityBuildingDetailDTO createEmptyDTO(Building building) {
        MonthlyUtilityBuildingDetailDTO emptyDto = new MonthlyUtilityBuildingDetailDTO();
        emptyDto.setBuildingID(building.getId());
        emptyDto.setBuildingName(building.getName());
        emptyDto.setUtilityGroupName(Collections.emptyMap());
        return emptyDto;
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

        Map<String, List<MonthlyUtilityBuildingUsageSummary>> monthlyBreakdown = aggregatedData.entrySet().stream()
                .flatMap(utilityGroup ->
                        utilityGroup.getValue().entrySet().stream()
                                .map(monthEntry -> {
                                    MonthlyUtilityBuildingUsageSummary dto =
                                            toMonthlyUtilityUsageSummaryDTO(monthEntry.getKey(), monthEntry.getValue());
                                    return new AbstractMap.SimpleEntry<>(utilityGroup.getKey(), dto);
                                })
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        Map<String, List<MonthlyUtilityBuildingUsageSummary>> finalBreakdown =
                fillMissingMonths(monthlyBreakdown);


        MonthlyUtilityBuildingDetailDTO buildingDetail = new MonthlyUtilityBuildingDetailDTO();
        buildingDetail.setBuildingID(building.getId());
        buildingDetail.setBuildingName(buildingName);
        buildingDetail.setUtilityGroupName(finalBreakdown);

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

    private List<MonthlyUtilityBuildingUsageSummary> fillSingleUtilityMonths(
            List<MonthlyUtilityBuildingUsageSummary> monthlyData) {

        Map<String, MonthlyUtilityBuildingUsageSummary> existingData = monthlyData.stream()
                .collect(Collectors.toMap(
                        MonthlyUtilityBuildingUsageSummary::getMonth,
                        data -> data
                ));

        List<MonthlyUtilityBuildingUsageSummary> fullYearData = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (existingData.containsKey(monthName)) {
                fullYearData.add(existingData.get(monthName));
            } else {
                MonthlyUtilityBuildingUsageSummary emptyUsage = new MonthlyUtilityBuildingUsageSummary();
                emptyUsage.setMonth(monthName);
                emptyUsage.setTotalUsageAmount(BigDecimal.ZERO);
                fullYearData.add(emptyUsage);
            }
        }
        return fullYearData;
    }


    private Map<String, List<MonthlyUtilityBuildingUsageSummary>> fillMissingMonths(
            Map<String, List<MonthlyUtilityBuildingUsageSummary>> currentBreakdown) {

        return currentBreakdown.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> fillSingleUtilityMonths(entry.getValue())
                        )
                );
    }
}
