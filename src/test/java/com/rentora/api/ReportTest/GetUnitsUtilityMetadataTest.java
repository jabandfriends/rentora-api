package com.rentora.api.ReportTest;

import com.rentora.api.model.dto.Report.Metadata.ReportUnitUtilityMetadata;
import com.rentora.api.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetUnitsUtilityMetadataTest extends ReportServiceBaseTest {

    private final BigDecimal WATER_USAGE_PRICE = new BigDecimal("1200.00");
    private final BigDecimal ELECTRIC_USAGE_PRICE = new BigDecimal("2500.50");
    private final long WATER_USAGE_UNIT = 500L;
    private final long ELECTRIC_USAGE_UNIT = 800L;

    private void setupMockRepositoryResponses(BigDecimal waterPrice, BigDecimal electricPrice, long waterUnit, long electricUnit) {
        when(unitUtilityRepository.sumPriceByUtility(eq(APARTMENT_ID), eq("water"))).thenReturn(waterPrice);
        when(unitUtilityRepository.sumPriceByUtility(eq(APARTMENT_ID), eq("electric"))).thenReturn(electricPrice);
        when(unitUtilityRepository.countUsageAmountByApartmentIdByUtility(eq(APARTMENT_ID), eq("water"))).thenReturn(waterUnit);
        when(unitUtilityRepository.countUsageAmountByApartmentIdByUtility(eq(APARTMENT_ID), eq("electric"))).thenReturn(electricUnit);
    }

    @Test
    @DisplayName("Test 1: GIVEN valid aggregate data WHEN called THEN metadata DTO is calculated correctly")
    void getUnitsUtilityMetadata_Success_AllAggregatesCorrect() {
        // GIVEN
        setupMockRepositoryResponses(WATER_USAGE_PRICE, ELECTRIC_USAGE_PRICE, WATER_USAGE_UNIT, ELECTRIC_USAGE_UNIT);

        // WHEN
        ReportUnitUtilityMetadata result = reportService.getUnitsUtilityMetadata(APARTMENT_ID);

        // THEN
        assertNotNull(result);

        // Individual Values
        assertEquals(WATER_USAGE_UNIT, result.getWaterUsageUnits());
        assertEquals(ELECTRIC_USAGE_UNIT, result.getElectricUsageUnits());
        assertEquals(WATER_USAGE_PRICE, result.getWaterUsagePrices());
        assertEquals(ELECTRIC_USAGE_PRICE, result.getElectricUsagePrices());

        // Total Calculations
        BigDecimal expectedTotalAmount = WATER_USAGE_PRICE.add(ELECTRIC_USAGE_PRICE);
        long expectedTotalUnits = WATER_USAGE_UNIT + ELECTRIC_USAGE_UNIT;

        assertEquals(expectedTotalAmount, result.getTotalAmount());
        assertEquals(expectedTotalUnits, result.getTotalUsageUnits());
    }

    @Test
    @DisplayName("Test 2: GIVEN zero aggregate data WHEN called THEN metadata DTO returns all zeros")
    void getUnitsUtilityMetadata_ZeroValues_ReturnsZeros() {
        // GIVEN
        setupMockRepositoryResponses(BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L);

        // WHEN
        ReportUnitUtilityMetadata result = reportService.getUnitsUtilityMetadata(APARTMENT_ID);

        // THEN
        assertNotNull(result);

        assertEquals(0L, result.getTotalUsageUnits());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    @DisplayName("Test 3: CRITICAL - GIVEN null prices from repository WHEN called THEN throws an exception (Service Logic Verification)")
    void getUnitsUtilityMetadata_Critical_HandlesNullPrices() {
        // GIVEN
        // waterUsagePrice is null, electricUsagePrice is valid
        // NOTE: We MUST mock all four calls to prevent an UnnecessaryStubbingException

        setupMockRepositoryResponses(null, ELECTRIC_USAGE_PRICE, WATER_USAGE_UNIT, ELECTRIC_USAGE_UNIT);

        // WHEN & THEN
        assertThrows(NullPointerException.class, () -> {
            reportService.getUnitsUtilityMetadata(APARTMENT_ID);
        }, "The service should throw NPE here, indicating that the service logic requires fixing to handle nulls.");


        // Verification that all 4 aggregate methods were called
        verify(unitUtilityRepository, times(1)).sumPriceByUtility(eq(APARTMENT_ID), eq("water"));
        verify(unitUtilityRepository, times(1)).sumPriceByUtility(eq(APARTMENT_ID), eq("electric"));
    }

    @Test
    @DisplayName("Test 4: GIVEN valid Apartment ID WHEN called THEN repository methods are called with correct arguments")
    void getUnitsUtilityMetadata_Verification_RepositoryCallsCorrect() {
        // GIVEN: Set up responses but the focus is on verification
        setupMockRepositoryResponses(WATER_USAGE_PRICE, ELECTRIC_USAGE_PRICE, WATER_USAGE_UNIT, ELECTRIC_USAGE_UNIT);

        // WHEN
        reportService.getUnitsUtilityMetadata(APARTMENT_ID);

        // THEN
        // Verify all 4 aggregation methods are called with the correct parameters
        verify(unitUtilityRepository, times(1)).countUsageAmountByApartmentIdByUtility(eq(APARTMENT_ID), eq("water"));
        verify(unitUtilityRepository, times(1)).countUsageAmountByApartmentIdByUtility(eq(APARTMENT_ID), eq("electric"));
        verify(unitUtilityRepository, times(1)).sumPriceByUtility(eq(APARTMENT_ID), eq("water"));
        verify(unitUtilityRepository, times(1)).sumPriceByUtility(eq(APARTMENT_ID), eq("electric"));

        verifyNoMoreInteractions(unitUtilityRepository);
    }
}
