package com.rentora.api.service;

import com.rentora.api.model.dto.ApartmentUtility.response.ApartmentUtilityMonthlyUsage;
import com.rentora.api.model.dto.ApartmentUtility.response.ApartmentUtilityUsageSummaryDTO;
import com.rentora.api.model.dto.ApartmentUtility.response.YearlyUtilityDetailDTO;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.model.entity.UnitUtilities;
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
public class ApartmentUtilityService {

    private final UnitUtilityRepository unitUtilityRepository;

    private static final String ELECTRIC = "electric";
    private static final String WATER = "water";

    public ApartmentUtilityUsageSummaryDTO getUtilitySummary(
            UUID apartmentId,
            int year) {

        List<UnitUtilities> rawEntities = unitUtilityRepository.findAllByApartmentIdAndYear(apartmentId, year);

        if (rawEntities.isEmpty()) {
            return createEmptySummary(apartmentId);
        }

        Map<String, Map<Month, BigDecimal>> aggregatedData =
                aggregateUtilitiesByMonthAndType(rawEntities);

        Map<String, List<ApartmentUtilityMonthlyUsage>> monthlyBreakdown =
                mapAggregatedDataToMonthlyBreakdown(aggregatedData);

        Map<String, List<ApartmentUtilityMonthlyUsage>> finalBreakdown =
                fillMissingMonths(monthlyBreakdown);

        Map<String, BigDecimal> totalUsageMap = calculateTotals(finalBreakdown);

        return ApartmentUtilityUsageSummaryDTO.builder()
                .apartmentId(apartmentId)
                .totalUsage(totalUsageMap)
                .monthlyBreakdown(finalBreakdown)
                .build();
    }

    public List<YearlyUtilityDetailDTO> getYearlyUtilityDetail(UUID apartmentId) {

        List<Object[]> rawData = unitUtilityRepository.findYearlyUsageSummary(apartmentId);

        if (rawData.isEmpty()) {
            return Collections.emptyList();
        }


        Map<Integer, List<Object[]>> usageByYear = rawData.stream()
                .collect(Collectors.groupingBy(row -> ((Number) row[0]).intValue()));

        return usageByYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int year = entry.getKey();

                    Map<String, BigDecimal> usageTotals = entry.getValue().stream()
                            .collect(Collectors.toMap(
                                    row -> {
                                        String name = ((String) row[1]).toLowerCase(Locale.ENGLISH);
                                        if (name.contains("electric")) return "electric";
                                        if (name.contains("water")) return "water";
                                        return name;
                                    },
                                    row -> (BigDecimal) row[2],
                                    BigDecimal::add
                            ));

                    return YearlyUtilityDetailDTO.builder()
                            .year(year)
                            .usageTotals(usageTotals)
                            .build();
                })
                .collect(Collectors.toList());
    }


    private Map<String, Map<Month, BigDecimal>> aggregateUtilitiesByMonthAndType(
            List<UnitUtilities> unitUtilities) {

        return unitUtilities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> {
                                    String utility = entity.getUtility().getUtilityName().toLowerCase(Locale.ENGLISH);
                                    if (utility.contains("electric")) return ELECTRIC;
                                    if (utility.contains("water")) return WATER;
                                    return utility;
                                },
                                Collectors.groupingBy(
                                        entity -> entity.getUsageMonth().getMonth(),
                                        Collectors.reducing(BigDecimal.ZERO, UnitUtilities::getUsageAmount, BigDecimal::add)
                                )
                        )
                );
    }

    private Map<String, List<ApartmentUtilityMonthlyUsage>> mapAggregatedDataToMonthlyBreakdown(
            Map<String, Map<Month, BigDecimal>> aggregatedData) {

        return aggregatedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().entrySet().stream()
                                        .map(monthEntry -> toMonthlyDataPoint(
                                                monthEntry.getKey().getValue(),
                                                monthEntry.getValue()
                                        ))
                                        .collect(Collectors.toList())
                        )
                );
    }

    private ApartmentUtilityMonthlyUsage toMonthlyDataPoint(int monthNumber, BigDecimal usageAmount) {
        String monthName = Month.of(monthNumber).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return ApartmentUtilityMonthlyUsage.builder()
                .month(monthName)
                .usageAmount(usageAmount)
                .build();
    }

    private Map<String, List<ApartmentUtilityMonthlyUsage>> fillMissingMonths(
            Map<String, List<ApartmentUtilityMonthlyUsage>> currentBreakdown) {

        Map<String, List<ApartmentUtilityMonthlyUsage>> finalMap = new HashMap<>();

        finalMap.put(ELECTRIC, fillSingleUtilityMonths(currentBreakdown.getOrDefault(ELECTRIC, Collections.emptyList())));
        finalMap.put(WATER, fillSingleUtilityMonths(currentBreakdown.getOrDefault(WATER, Collections.emptyList())));

        return finalMap;
    }

    private List<ApartmentUtilityMonthlyUsage> fillSingleUtilityMonths(List<ApartmentUtilityMonthlyUsage> monthlyData) {

        Map<String, ApartmentUtilityMonthlyUsage> existingData = monthlyData.stream()
                .collect(Collectors.toMap(
                        ApartmentUtilityMonthlyUsage::getMonth,
                        data -> data
                ));

        List<ApartmentUtilityMonthlyUsage> fullYearData = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (existingData.containsKey(monthName)) {
                fullYearData.add(existingData.get(monthName));
            } else {
                fullYearData.add(ApartmentUtilityMonthlyUsage.builder()
                        .month(monthName)
                        .usageAmount(BigDecimal.ZERO)
                        .build());
            }
        }
        return fullYearData;
    }

    private Map<String, BigDecimal> calculateTotals(Map<String, List<ApartmentUtilityMonthlyUsage>> finalBreakdown) {

        Map<String, BigDecimal> totals = new HashMap<>();

        finalBreakdown.forEach((utilityType, monthlyData) -> {
            BigDecimal total = monthlyData.stream()
                    .map(ApartmentUtilityMonthlyUsage::getUsageAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totals.put(utilityType, total);
        });

        return totals;
    }

    private ApartmentUtilityUsageSummaryDTO createEmptySummary(UUID apartmentId) {
        Map<String, BigDecimal> emptyTotals = Map.of(
                ELECTRIC, BigDecimal.ZERO,
                WATER, BigDecimal.ZERO
        );

        List<ApartmentUtilityMonthlyUsage> emptyYearData = fillSingleUtilityMonths(Collections.emptyList());

        return ApartmentUtilityUsageSummaryDTO.builder()
                .apartmentId(apartmentId)
                .totalUsage(emptyTotals)
                .monthlyBreakdown(Map.of(
                        ELECTRIC, emptyYearData,
                        WATER, emptyYearData
                ))
                .build();
    }
}