package com.rentora.api.UtilityTest;

import com.rentora.api.model.dto.Utility.Request.UpdateUtilityDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.UtilityRepository;
import com.rentora.api.service.UtilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient; // Import lenient

@ExtendWith(MockitoExtension.class)
public abstract class UtilityServiceBaseTest {

    @Mock
    protected UtilityRepository utilityRepository;
    @Mock
    protected ApartmentRepository apartmentRepository;

    @InjectMocks
    protected UtilityService utilityService;

    // UUID Constants
    protected final UUID APARTMENT_ID = UUID.fromString("11111111-0000-0000-0000-000000000000");
    protected final UUID WATER_UTILITY_ID = UUID.fromString("22222222-0000-0000-0000-000000000001");
    protected final UUID ELECTRIC_UTILITY_ID = UUID.fromString("22222222-0000-0000-0000-000000000002");
    protected final UUID NON_EXISTENT_ID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

    // Mock Entities and DTOs
    protected Apartment mockApartment;
    protected Utility mockWaterUtility;
    protected Utility mockElectricUtility;
    protected UpdateUtilityDto mockUpdateDto;

    @BeforeEach
    void setUpBase() {
        // 1. Setup Apartment
        mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);
        mockApartment.setName("Sample Apartment");

        // 2. Setup Water Utility
        mockWaterUtility = new Utility();
        mockWaterUtility.setId(WATER_UTILITY_ID);
        mockWaterUtility.setApartment(mockApartment);
        mockWaterUtility.setUtilityName("water");
        // NOTE: Adjusted to 'fixed' for the water DTO test in GetUtility...Test (as shown in the last correct code)
        mockWaterUtility.setUtilityType(Utility.UtilityType.fixed);
        mockWaterUtility.setCategory(Utility.Category.utility);
        mockWaterUtility.setUnitPrice(new BigDecimal("20.00"));
        mockWaterUtility.setFixedPrice(new BigDecimal("0.00"));

        // 3. Setup Electric Utility
        mockElectricUtility = new Utility();
        mockElectricUtility.setId(ELECTRIC_UTILITY_ID);
        mockElectricUtility.setApartment(mockApartment);
        mockElectricUtility.setUtilityName("electric");
        // NOTE: Adjusted to 'unit' for the electric DTO test in GetUtility...Test (as shown in the last correct code)
        mockElectricUtility.setUtilityType(Utility.UtilityType.meter);
        mockElectricUtility.setCategory(Utility.Category.utility);
        mockElectricUtility.setUnitPrice(new BigDecimal("7.50"));
        mockElectricUtility.setFixedPrice(new BigDecimal("100.00"));

        // 4. Setup Update DTO (Mocked to be used with when/thenReturn for getters)
        mockUpdateDto = mock(UpdateUtilityDto.class);
        // Using lenient() to avoid UnnecessaryStubbingException in GetUtilityByApartmentIdTest
        lenient().when(mockUpdateDto.getWaterUtilityId()).thenReturn(WATER_UTILITY_ID);
        lenient().when(mockUpdateDto.getElectricUtilityId()).thenReturn(ELECTRIC_UTILITY_ID);

        // Default update values for a common case
        lenient().when(mockUpdateDto.getWaterUtilityType()).thenReturn(Utility.UtilityType.fixed);
        lenient().when(mockUpdateDto.getWaterUtilityUnitPrice()).thenReturn(new BigDecimal("0.00"));
        lenient().when(mockUpdateDto.getWaterUtilityFixedPrice()).thenReturn(new BigDecimal("350.00"));

        lenient().when(mockUpdateDto.getElectricUtilityType()).thenReturn(Utility.UtilityType.meter);
        lenient().when(mockUpdateDto.getElectricUtilityUnitPrice()).thenReturn(new BigDecimal("9.00"));
        lenient().when(mockUpdateDto.getElectricUtilityFixedPrice()).thenReturn(new BigDecimal("0.00"));
    }
}