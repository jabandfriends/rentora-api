package com.rentora.api.UtilityTest;

import com.rentora.api.model.dto.Utility.Response.UtilitySummaryResponseDto;
import com.rentora.api.model.entity.Utility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetUtilityByApartmentIdTest extends UtilityServiceBaseTest {

    @Test
    @DisplayName("GIVEN existing utilities WHEN getUtilityByApartmentId is called THEN a list of DTOs is returned")
    void getUtilityByApartmentId_Success_ReturnsDtoList() {
        // GIVEN
        List<Utility> mockUtilityList = List.of(mockWaterUtility, mockElectricUtility);
        when(utilityRepository.findByApartmentId(APARTMENT_ID)).thenReturn(mockUtilityList);

        // WHEN
        List<UtilitySummaryResponseDto> result = utilityService.getUtilityByApartmentId(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify data of the first utility (Water)
        UtilitySummaryResponseDto waterDto = result.stream()
                .filter(dto -> dto.getUtilityName().equals(mockWaterUtility.getUtilityName()))
                .findFirst()
                .orElseThrow();
        assertEquals(WATER_UTILITY_ID, waterDto.getUtilityId());
        assertEquals("water", waterDto.getUtilityName());
        assertEquals(Utility.UtilityType.fixed, waterDto.getUtilityType());
        assertEquals(mockWaterUtility.getFixedPrice(), waterDto.getUtilityFixedPrice());
        assertEquals(mockWaterUtility.getUnitPrice(), waterDto.getUtilityUnitPrice());

        // Verify data of the second utility (Electric)
        UtilitySummaryResponseDto electricDto = result.stream()
                .filter(dto -> dto.getUtilityName().equals(mockElectricUtility.getUtilityName()))
                .findFirst()
                .orElseThrow();
        assertEquals(ELECTRIC_UTILITY_ID, electricDto.getUtilityId());
        assertEquals("electric", electricDto.getUtilityName());
        assertEquals(Utility.UtilityType.meter, electricDto.getUtilityType());
        assertEquals(mockElectricUtility.getFixedPrice(), electricDto.getUtilityFixedPrice());
        assertEquals(mockElectricUtility.getUnitPrice(), electricDto.getUtilityUnitPrice());

        verify(utilityRepository).findByApartmentId(APARTMENT_ID);
        verifyNoMoreInteractions(utilityRepository, apartmentRepository);
    }

    @Test
    @DisplayName("GIVEN no utilities exist WHEN getUtilityByApartmentId is called THEN an empty list is returned")
    void getUtilityByApartmentId_NoUtilityFound_ReturnsEmptyList() {
        // GIVEN
        when(utilityRepository.findByApartmentId(APARTMENT_ID)).thenReturn(Collections.emptyList());

        // WHEN
        List<UtilitySummaryResponseDto> result = utilityService.getUtilityByApartmentId(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(utilityRepository).findByApartmentId(APARTMENT_ID);
        verifyNoMoreInteractions(utilityRepository, apartmentRepository);
    }
}