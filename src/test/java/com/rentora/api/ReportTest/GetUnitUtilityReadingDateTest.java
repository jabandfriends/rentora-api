package com.rentora.api.ReportTest;

import com.rentora.api.model.dto.Report.Response.ReadingDateDto;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetUnitUtilityReadingDateTest extends ReportServiceBaseTest {

    private final LocalDate DATE_A = LocalDate.of(2025, 1, 1);
    private final LocalDate DATE_B = LocalDate.of(2025, 2, 1);
    private final LocalDate DATE_C = LocalDate.of(2024, 12, 1);

    private List<UnitUtilities> createMockUtilitiesWithDates(LocalDate date1, LocalDate date2, LocalDate date3, LocalDate date4) {
        UnitUtilities u1 = new UnitUtilities(); u1.setUsageMonth(date1);
        UnitUtilities u2 = new UnitUtilities(); u2.setUsageMonth(date2);
        UnitUtilities u3 = new UnitUtilities(); u3.setUsageMonth(date3);
        UnitUtilities u4 = new UnitUtilities(); u4.setUsageMonth(date4);
        return List.of(u1, u2, u3, u4);
    }

    @Test
    @DisplayName("Test 1: GIVEN multiple utility readings WHEN called THEN distinct dates are returned and sorted ascending")
    void getReadingDate_Success_DistinctAndSorted() {
        // GIVEN: Data includes duplicates and unsorted dates (DATE_A, DATE_B, DATE_C, DATE_A)
        List<UnitUtilities> mockList = createMockUtilitiesWithDates(DATE_A, DATE_B, DATE_C, DATE_A);

        when(unitUtilityRepository.findAll(any(Specification.class))).thenReturn(mockList);

        // WHEN
        List<ReadingDateDto> result = reportService.getUnitUtilityReadingDate(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size(), "Should return 3 distinct dates.");

        // Verify order is sorted: DATE_C (Dec), DATE_A (Jan), DATE_B (Feb)
        assertEquals(DATE_C, result.get(0).getReadingDate());
        assertEquals(DATE_A, result.get(1).getReadingDate());
        assertEquals(DATE_B, result.get(2).getReadingDate());

        verify(unitUtilityRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Test 2: GIVEN readings containing NULL dates WHEN called THEN null dates are filtered out correctly")
    void getReadingDate_HandlesNullAndEmptyDates() {
        // GIVEN: Data includes nulls, duplicates, and valid dates
        List<UnitUtilities> mockList = createMockUtilitiesWithDates(DATE_A, null, DATE_B, DATE_A);

        when(unitUtilityRepository.findAll(any(Specification.class))).thenReturn(mockList);

        // WHEN
        List<ReadingDateDto> result = reportService.getUnitUtilityReadingDate(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 distinct, non-null dates.");

        // Verify nulls are gone and order is maintained
        assertTrue(result.stream().noneMatch(dto -> dto.getReadingDate() == null));
        assertEquals(DATE_A, result.get(0).getReadingDate());
        assertEquals(DATE_B, result.get(1).getReadingDate());

        verify(unitUtilityRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Test 3: GIVEN no utilities found WHEN called THEN an empty list is returned")
    void getReadingDate_NoUtilitiesFound_ReturnsEmptyList() {
        // GIVEN
        when(unitUtilityRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // WHEN
        List<ReadingDateDto> result = reportService.getUnitUtilityReadingDate(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return an empty list.");

        verify(unitUtilityRepository).findAll(any(Specification.class));
    }
}