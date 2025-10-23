package com.rentora.api.service;

import com.rentora.api.model.dto.MonthlyUnitlity.Response.MonthlyUtilityGroupInfo;
import com.rentora.api.model.dto.MonthlyUnitlity.Response.MonthlyUtilityGroupNameDTO;
import com.rentora.api.model.dto.MonthlyUnitlity.Response.MonthlyUtilityUnitDetailDTO;
import com.rentora.api.model.dto.MonthlyUnitlity.Response.MonthlyUtilityUsageSummaryDTO;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.MonthlyUtilityRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.specifications.UnitUtilitySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityService {

    private final MonthlyUtilityRepository monthlyUtilityRepository;
    private final UnitUtilityRepository unitUtilityRepository;

    public MonthlyUtilityUnitDetailDTO getMonthlyUtilitySummary(UUID unitId) {

        List<UnitUtilities> entities = monthlyUtilityRepository.findAllByUnitId(unitId);

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

        MonthlyUtilityGroupInfo monthlyUtilityGroupInfo = new MonthlyUtilityGroupInfo();
        unitUtility.setUtilityGroups(monthlyUtilityGroupInfo);


        return unitUtility;
    }

    public


    public MonthlyUtilityGroupNameDTO toMonthlyUtilityNameDTO (List<UnitUtilities> unitUtilities) {


        List<MonthlyUtilityUsageSummaryDTO> monthlyUsages = new ArrayList();
        for (UnitUtilities unitUtility : unitUtilities) {

            String utilityName = unitUtility.getUtility().getUtilityName();

            MonthlyUtilityGroupNameDTO utilityGroupName = new MonthlyUtilityGroupNameDTO();
            utilityGroupName.setUtilityName(utilityName);


            MonthlyUtilityUsageSummaryDTO monthlyUsage = toMonthlyUtilityUsageSummaryDTO(unitUtility);
            monthlyUsages.add(monthlyUsage);
            utilityGroupName.setMonthlyUsages(monthlyUsages);

        }


        return ;
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
