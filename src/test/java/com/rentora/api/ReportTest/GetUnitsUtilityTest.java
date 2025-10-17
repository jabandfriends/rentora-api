package com.rentora.api.ReportTest;

import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUnitsUtilityTest extends ReportServiceBaseTest {

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    @DisplayName("Test 1: GIVEN unit utilities WHEN called THEN water/electric readings are grouped and costs/usages are calculated correctly")
    void groupingAndMapping_Success() {
        // GIVEN: Unit 1 readings (Water and Electric)
        List<UnitUtilities> unit1Readings = List.of(mockWaterReading1, mockElectricReading1);
        Page<UnitUtilities> mockPage = new PageImpl<>(unit1Readings, pageable, 2);

        when(unitUtilityRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // WHEN
        Page<ReportService.UnitServiceResponseDto> result = reportService.getUnitsUtility(APARTMENT_ID, UNIT_NAME_1, BUILDING_NAME, USAGE_MONTH_STRING, pageable);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getTotalElements(), "Should return 1 grouped unit.");
        ReportService.UnitServiceResponseDto dto = result.getContent().getFirst();

        // Verify Grouping and Calculation
        assertEquals(UNIT_NAME_1, dto.getRoomName());
        assertEquals(new BigDecimal("20.00"), dto.getWaterUsage(), "Water usage calculation should be 120.00 - 100.00 = 20.00");
        assertEquals(new BigDecimal("100.00"), dto.getElectricUsage(), "Electric usage calculation should be 2100.00 - 2000.00 = 100.00");
        assertEquals(new BigDecimal("500.00"), dto.getWaterCost());
        assertEquals(new BigDecimal("750.00"), dto.getElectricCost());
    }

    @Test
    @DisplayName("Test 2: GIVEN call with filter parameters WHEN getUnitsUtility is called THEN correct Specification is passed to repository")
    void specVerification_IsCalled() {
        // GIVEN
        Page<UnitUtilities> mockEmptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(unitUtilityRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockEmptyPage);

        // WHEN
        reportService.getUnitsUtility(APARTMENT_ID, UNIT_NAME_1, BUILDING_NAME, USAGE_MONTH_STRING, pageable);

        // THEN
        // Verify findAll is called with a Specification argument
        verify(unitUtilityRepository).findAll(any(Specification.class), eq(pageable));
        verify(contractRepository, never()).findActiveContractByUnitId(any(UUID.class));
    }

    @Test
    @DisplayName("Test 3: GIVEN unit with ACTIVE contract WHEN mapping THEN tenantName is set correctly")
    void mappingWithContract_TenantNameIsCorrect() {
        // GIVEN: Unit 1 readings (Has contract)
        List<UnitUtilities> unit1Readings = List.of(mockWaterReading1, mockElectricReading1);
        Page<UnitUtilities> mockPage = new PageImpl<>(unit1Readings, pageable, 2);

        when(unitUtilityRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);
        // Mock Contract lookup
        when(contractRepository.findActiveContractByUnitId(UNIT_ID_1)).thenReturn(Optional.of(mockContract1));

        // WHEN
        Page<ReportService.UnitServiceResponseDto> result = reportService.getUnitsUtility(APARTMENT_ID, UNIT_NAME_1, BUILDING_NAME, USAGE_MONTH_STRING, pageable);

        // THEN
        ReportService.UnitServiceResponseDto dto = result.getContent().getFirst();
        assertEquals(mockTenant1.getFullName(), dto.getTenantName(), "Tenant name should be pulled from active contract.");

        verify(contractRepository).findActiveContractByUnitId(UNIT_ID_1);
    }

    @Test
    @DisplayName("Test 4: GIVEN unit without contract WHEN mapping THEN tenantName defaults to 'No tenant'")
    void mappingNoContract_TenantNameIsDefault() {
        // GIVEN: Unit 2 readings (No contract)
        UnitUtilities waterReading2 = new UnitUtilities();
        waterReading2.setUnit(mockUnit2);
        waterReading2.setUtility(mockWater);
        // FIX: Initialize meters for calculation
        waterReading2.setMeterStart(new BigDecimal("10.00"));
        waterReading2.setMeterEnd(new BigDecimal("20.00"));
        waterReading2.setCalculatedCost(new BigDecimal("50.00"));

        UnitUtilities electricReading2 = new UnitUtilities();
        electricReading2.setUnit(mockUnit2);
        electricReading2.setUtility(mockElectric);
        // FIX: Initialize meters for calculation
        electricReading2.setMeterStart(new BigDecimal("100.00"));
        electricReading2.setMeterEnd(new BigDecimal("110.00"));
        electricReading2.setCalculatedCost(new BigDecimal("80.00"));

        List<UnitUtilities> unit2Readings = List.of(waterReading2, electricReading2);
        Page<UnitUtilities> mockPage = new PageImpl<>(unit2Readings, pageable, 2);

        when(unitUtilityRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);
        // Mock Contract lookup (Return Empty)
        when(contractRepository.findActiveContractByUnitId(UNIT_ID_2)).thenReturn(Optional.empty());

        // WHEN
        Page<ReportService.UnitServiceResponseDto> result = reportService.getUnitsUtility(APARTMENT_ID, mockUnit2.getUnitName(), BUILDING_NAME, USAGE_MONTH_STRING, pageable);

        // THEN
        ReportService.UnitServiceResponseDto dto = result.getContent().getFirst();
        assertEquals("No tenant", dto.getTenantName(), "Tenant name should default to 'No tenant' when no active contract is found.");

        // Assert calculation (which caused the NPE) is now correct
        assertEquals(new BigDecimal("10.00"), dto.getWaterUsage(), "Water usage calculation should be 20.00 - 10.00 = 10.00");

        verify(contractRepository).findActiveContractByUnitId(UNIT_ID_2);
    }
}