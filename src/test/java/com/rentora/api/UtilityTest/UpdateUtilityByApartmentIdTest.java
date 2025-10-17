package com.rentora.api.UtilityTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.Utility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UpdateUtilityByApartmentIdTest extends UtilityServiceBaseTest {

    private void setupSuccessfulUtilityLookup() {
        // Mock Apartment lookup
        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(mockApartment));
        // Mock Water and Electric utility lookups by ID and Apartment
        when(utilityRepository.findByIdAndApartment(WATER_UTILITY_ID, mockApartment)).thenReturn(Optional.of(mockWaterUtility));
        when(utilityRepository.findByIdAndApartment(ELECTRIC_UTILITY_ID, mockApartment)).thenReturn(Optional.of(mockElectricUtility));
    }

    @Test
    @DisplayName("GIVEN valid request with all fields WHEN updateUtilityByApartmentId is called THEN both utilities are updated and saved")
    void updateUtility_Success_AllFieldsUpdatedAndSaved() {
        // GIVEN
        setupSuccessfulUtilityLookup();
        // DTO stubbing is set up in BaseTest for success case

        // WHEN
        utilityService.updateUtilityByApartmentId(APARTMENT_ID, mockUpdateDto);

        // THEN
        ArgumentCaptor<List<Utility>> captor = ArgumentCaptor.forClass(List.class);
        verify(utilityRepository).saveAll(captor.capture());

        List<Utility> savedUtilities = captor.getValue();
        assertEquals(2, savedUtilities.size());

        // Verify Water update
        Utility savedWater = savedUtilities.stream().filter(u -> u.getId().equals(WATER_UTILITY_ID)).findFirst().orElseThrow();
        assertEquals(mockUpdateDto.getWaterUtilityType(), savedWater.getUtilityType());
        assertEquals(mockUpdateDto.getWaterUtilityUnitPrice(), savedWater.getUnitPrice());
        assertEquals(mockUpdateDto.getWaterUtilityFixedPrice(), savedWater.getFixedPrice());

        // Verify Electric update
        Utility savedElectric = savedUtilities.stream().filter(u -> u.getId().equals(ELECTRIC_UTILITY_ID)).findFirst().orElseThrow();
        assertEquals(mockUpdateDto.getElectricUtilityType(), savedElectric.getUtilityType());
        assertEquals(mockUpdateDto.getElectricUtilityUnitPrice(), savedElectric.getUnitPrice());
        assertEquals(mockUpdateDto.getElectricUtilityFixedPrice(), savedElectric.getFixedPrice());

        verify(apartmentRepository).findById(APARTMENT_ID);
        verify(utilityRepository, times(2)).findByIdAndApartment(any(UUID.class), eq(mockApartment));
        verifyNoMoreInteractions(apartmentRepository, utilityRepository);
    }

    @Test
    @DisplayName("GIVEN request with partial fields (Electric Unit Price) WHEN updateUtilityByApartmentId is called THEN only specified fields are updated")
    void updateUtility_Success_PartialFieldsUpdated() {
        // GIVEN
        setupSuccessfulUtilityLookup();

        // Stub DTO to return only one non-null update field
        // NOTE: Stubbing must be used with 'when' here as we override the lenient setup from BaseTest
        when(mockUpdateDto.getElectricUtilityUnitPrice()).thenReturn(new BigDecimal("10.50"));
        when(mockUpdateDto.getElectricUtilityType()).thenReturn(null);
        when(mockUpdateDto.getElectricUtilityFixedPrice()).thenReturn(null);
        when(mockUpdateDto.getWaterUtilityType()).thenReturn(null);
        when(mockUpdateDto.getWaterUtilityUnitPrice()).thenReturn(null);
        when(mockUpdateDto.getWaterUtilityFixedPrice()).thenReturn(null);

        // Store original values before the update call to verify they remain unchanged
        Utility.UtilityType originalWaterType = mockWaterUtility.getUtilityType();
        BigDecimal originalElectricFixedPrice = mockElectricUtility.getFixedPrice();

        // WHEN
        utilityService.updateUtilityByApartmentId(APARTMENT_ID, mockUpdateDto);

        // THEN
        ArgumentCaptor<List<Utility>> captor = ArgumentCaptor.forClass(List.class);
        verify(utilityRepository).saveAll(captor.capture());

        List<Utility> savedUtilities = captor.getValue();
        Utility savedElectric = savedUtilities.stream().filter(u -> u.getId().equals(ELECTRIC_UTILITY_ID)).findFirst().orElseThrow();
        Utility savedWater = savedUtilities.stream().filter(u -> u.getId().equals(WATER_UTILITY_ID)).findFirst().orElseThrow();

        // Verify Electric update
        assertEquals(new BigDecimal("10.50"), savedElectric.getUnitPrice()); // UPDATED
        assertEquals(originalElectricFixedPrice, savedElectric.getFixedPrice()); // UNCHANGED (original value)

        // Verify Water remains UNCHANGED
        assertEquals(originalWaterType, savedWater.getUtilityType()); // UNCHANGED (original value)

        verify(apartmentRepository).findById(APARTMENT_ID);
        verify(utilityRepository, times(2)).findByIdAndApartment(any(UUID.class), eq(mockApartment));
    }

    @Test
    @DisplayName("GIVEN Apartment ID not found WHEN updateUtilityByApartmentId is called THEN ResourceNotFoundException is thrown")
    void updateUtility_ApartmentNotFound_ThrowsResourceNotFound() {
        // GIVEN
        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> utilityService.updateUtilityByApartmentId(APARTMENT_ID, mockUpdateDto),
                "Apartment not found");

        verify(apartmentRepository).findById(APARTMENT_ID);
        verifyNoInteractions(utilityRepository);
    }

    @Test
    @DisplayName("GIVEN Water Utility ID not found WHEN updateUtilityByApartmentId is called THEN ResourceNotFoundException is thrown")
    void updateUtility_WaterUtilityNotFound_ThrowsResourceNotFound() {
        // GIVEN
        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(mockApartment));
        when(utilityRepository.findByIdAndApartment(WATER_UTILITY_ID, mockApartment)).thenReturn(Optional.empty()); // Water not found

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> utilityService.updateUtilityByApartmentId(APARTMENT_ID, mockUpdateDto),
                "Water Utility not found");

        verify(apartmentRepository).findById(APARTMENT_ID);
        verify(utilityRepository).findByIdAndApartment(WATER_UTILITY_ID, mockApartment);
        verifyNoMoreInteractions(utilityRepository);
    }

    @Test
    @DisplayName("GIVEN Electric Utility ID not found WHEN updateUtilityByApartmentId is called THEN ResourceNotFoundException is thrown")
    void updateUtility_ElectricUtilityNotFound_ThrowsResourceNotFound() {
        // GIVEN
        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(mockApartment));
        when(utilityRepository.findByIdAndApartment(WATER_UTILITY_ID, mockApartment)).thenReturn(Optional.of(mockWaterUtility));
        when(utilityRepository.findByIdAndApartment(ELECTRIC_UTILITY_ID, mockApartment)).thenReturn(Optional.empty()); // Electric not found

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> utilityService.updateUtilityByApartmentId(APARTMENT_ID, mockUpdateDto),
                "Electric Utility not found");

        verify(apartmentRepository).findById(APARTMENT_ID);
        verify(utilityRepository).findByIdAndApartment(WATER_UTILITY_ID, mockApartment);
        verify(utilityRepository).findByIdAndApartment(ELECTRIC_UTILITY_ID, mockApartment);
        verifyNoMoreInteractions(utilityRepository);
    }
}
