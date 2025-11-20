package com.rentora.api.service;

import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingDetailDTO;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingUsageSummary;
import com.rentora.api.model.dto.MonthlyUtilityFloor.Metadata.MonthlyUtilityFloorMetadata;
import com.rentora.api.model.dto.MonthlyUtilityFloor.Respose.MonthlyUtilityFloorDetailDto;
import com.rentora.api.model.dto.MonthlyUtilityFloor.Respose.MonthlyUtilityFloorUsageSummary;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.MonthlyUtilityBuildingRepository;
import com.rentora.api.repository.FloorRepository;
import com.rentora.api.repository.MonthlyUtilityFloorRepository;
import com.rentora.api.specifications.MonthlyUtilityFloorSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.rentora.api.specifications.MonthlyUtilityFloorSpecification.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityFloorService {

    private final MonthlyUtilityFloorRepository monthlyUtilityFloorRepository;
    private final FloorRepository floorRepository;

    public MonthlyUtilityFloorMetadata getMonthlyUtilityFloorMetadata (
            Apartment apartment,
            List<MonthlyUtilityFloorDetailDto> monthlyUtilityFloor) {

        UUID apartmentId = apartment.getId();

        long totalFloorCount = floorRepository.count();

        return MonthlyUtilityFloorMetadata.builder().totalUtilityFloor(totalFloorCount).build();

    }

    public Page<MonthlyUtilityFloorDetailDto> getApartmentUtilitySummaryByFloor(
            Apartment apartment,
            Building building,
            UUID floorId,
            Pageable pageable) {

        Page<Floor> Floors;

        Specification<Floor> spec = MonthlyUtilityFloorSpecification.hasApartment(apartment);

        if (building != null) {
            spec = spec.and(MonthlyUtilityFloorSpecification.hasBuilding(building));
        }

        if (floorId != null) {
            spec = spec.and(MonthlyUtilityFloorSpecification.hasFloorId(floorId));
        }

       Floors = floorRepository.findAll(spec, pageable);

        return Floors.map(floor ->
                this.toFloorSummaryWithAggregation(floor, apartment)
        );
    }

    private MonthlyUtilityFloorDetailDto toFloorSummaryWithAggregation(
            Floor floor,
            Apartment apartment) {

        List<UnitUtilities> entities = monthlyUtilityFloorRepository
                .findAllByUnit_Floor(floor);

        if (entities.isEmpty()) {
            return createEmptyDTO(floor);
        }

        Map<String, Map<LocalDate, BigDecimal>> aggregatedData =
                aggregateUtilitiesByMonthAndType(entities);

        return toMonthlyUtilityFloorDetailDTO(floor, aggregatedData);
    }

    private MonthlyUtilityFloorDetailDto createEmptyDTO(Floor floor) {
        MonthlyUtilityFloorDetailDto emptyDto = new MonthlyUtilityFloorDetailDto();
        emptyDto.setBuildingId(floor.getBuilding().getId());
        emptyDto.setBuildingName(floor.getBuilding().getName());
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

    private MonthlyUtilityFloorDetailDto toMonthlyUtilityFloorDetailDTO(
            Floor floor,
            Map<String, Map<LocalDate, BigDecimal>> aggregatedData) {

        String buildingName = floor.getBuilding().getName();
        String FloorName = floor.getFloorName();
        Integer FloorNumber = floor.getFloorNumber();

        Map<String, List<MonthlyUtilityFloorUsageSummary>> monthlyBreakdown = aggregatedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                utilityGroup -> utilityGroup.getValue().entrySet().stream()
                                        .map(monthEntry ->
                                                toMonthlyFloorUsageSummaryDTO(monthEntry.getKey(), monthEntry.getValue())
                                        )
                                        .collect(Collectors.toList())
                        )
                );

        // ** INTEGRATE: Zero-fill logic using fillMissingMonths **
        Map<String, List<MonthlyUtilityFloorUsageSummary>> finalBreakdown =
                fillMissingMonths(monthlyBreakdown);


        MonthlyUtilityFloorDetailDto floorDetail = new MonthlyUtilityFloorDetailDto();
        floorDetail.setBuildingId(floor.getBuilding().getId());
        floorDetail.setFloorName(FloorName);
        floorDetail.setFloorNumber(FloorNumber);
        floorDetail.setBuildingName(buildingName);
        floorDetail.setUtilityGroupName(finalBreakdown);

        return floorDetail;
    }

    public MonthlyUtilityFloorUsageSummary toMonthlyFloorUsageSummaryDTO(
            LocalDate month,
            BigDecimal totalUsage) {

        MonthlyUtilityFloorUsageSummary monthlyUsage = new MonthlyUtilityFloorUsageSummary();

        String monthName = month.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        monthlyUsage.setMonth(monthName);
        monthlyUsage.setTotalFloorUsage(totalUsage);

        return monthlyUsage;
    }

    private Map<String, List<MonthlyUtilityFloorUsageSummary>> fillMissingMonths(
            Map<String, List<MonthlyUtilityFloorUsageSummary>> currentBreakdown) {

        return currentBreakdown.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> fillSingleUtilityMonths(entry.getValue())
                        ));
    }

    private List<MonthlyUtilityFloorUsageSummary> fillSingleUtilityMonths(List<MonthlyUtilityFloorUsageSummary> monthlyData) {

        Map<String, MonthlyUtilityFloorUsageSummary> existingData = monthlyData.stream()
                .collect(Collectors.toMap(
                        MonthlyUtilityFloorUsageSummary::getMonth,
                        data -> data
                ));

        List<MonthlyUtilityFloorUsageSummary> fullYearData = new ArrayList<>();

        for (int i = 1; i < 13; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (existingData.containsKey(monthName)) {
                fullYearData.add(existingData.get(monthName));
            } else {
                MonthlyUtilityFloorUsageSummary emptyUsage = new MonthlyUtilityFloorUsageSummary();
                emptyUsage.setMonth(monthName);
                emptyUsage.setTotalFloorUsage(BigDecimal.ZERO);
                fullYearData.add(emptyUsage);
            }
        }
        return fullYearData;
    }
}