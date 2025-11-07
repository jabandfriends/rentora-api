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
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.rentora.api.specifications.MonthlyUtilityFloorSpecification.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityFloorService {

    private final MonthlyUtilityBuildingRepository monthlyUtilityBuildingRepository;
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
            String search,
            Pageable pageable) {

        Page<Floor> Floors;

        Specification<Floor> spec = MonthlyUtilityFloorSpecification.hasFloorName(search).and(hasBuilding(building).and(hasApartment(apartment)));

       Floors = floorRepository.findAll(spec, pageable);

        return Floors.map(floor ->
                this.toFloorSummaryWithAggregation(floor, apartment)
        );
    }

    private MonthlyUtilityFloorDetailDto toFloorSummaryWithAggregation(
            Floor floor,
            Apartment apartment) {

        List<UnitUtilities> entities = monthlyUtilityBuildingRepository
                .findAllByUnit_Floor_Building_Apartment(apartment);

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

        Map<String, List<MonthlyUtilityFloorUsageSummary>> utilityGroupName = aggregatedData.entrySet().stream()
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

        MonthlyUtilityFloorDetailDto floorDetail = new MonthlyUtilityFloorDetailDto();
        floorDetail.setBuildingId(floor.getBuilding().getId());
        floorDetail.setFloorName(FloorName);
        floorDetail.setBuildingName(buildingName);
        floorDetail.setUtilityGroupName(utilityGroupName);

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
}