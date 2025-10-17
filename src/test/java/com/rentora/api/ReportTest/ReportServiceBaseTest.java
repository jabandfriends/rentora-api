package com.rentora.api.ReportTest;

import com.rentora.api.model.entity.*;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class ReportServiceBaseTest {

    @Mock
    protected UnitUtilityRepository unitUtilityRepository;
    @Mock
    protected ContractRepository contractRepository;

    @InjectMocks
    protected ReportService reportService;

    // UUID Constants
    protected final UUID APARTMENT_ID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    protected final UUID UNIT_ID_1 = UUID.fromString("40000000-0000-0000-0000-000000000001");
    protected final UUID UNIT_ID_2 = UUID.fromString("40000000-0000-0000-0000-000000000002");
    protected final UUID CONTRACT_ID_1 = UUID.fromString("60000000-0000-0000-0000-000000000001");
    protected final UUID WATER_UTIL_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");
    protected final UUID ELECTRIC_UTIL_ID = UUID.fromString("70000000-0000-0000-0000-000000000002");

    // String Constants
    protected final String BUILDING_NAME = "Building A";
    protected final String UNIT_NAME_1 = "ROOM A101";

    // Dates
    protected final LocalDate USAGE_MONTH = LocalDate.of(2025, 10, 1);
    protected final String USAGE_MONTH_STRING = "2025-10-01";
    protected final LocalDate ANOTHER_MONTH = LocalDate.of(2025, 9, 1);

    // Mock Entities
    protected Apartment mockApartment;
    protected Building mockBuilding;
    protected Floor mockFloor;
    protected Unit mockUnit1;
    protected Unit mockUnit2;
    protected Contract mockContract1;
    protected User mockTenant1;
    protected Utility mockWater;
    protected Utility mockElectric;
    protected UnitUtilities mockWaterReading1;
    protected UnitUtilities mockElectricReading1;


    @BeforeEach
    void setUpBase() {
        // Core Entities setup
        mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);
        mockApartment.setName("The Sanctuary");

        mockBuilding = new Building();
        mockBuilding.setName(BUILDING_NAME);
        mockBuilding.setApartment(mockApartment);

        mockFloor = new Floor();
        mockFloor.setFloorName("Floor 1");
        mockFloor.setBuilding(mockBuilding);

        // Unit 1
        mockUnit1 = new Unit();
        mockUnit1.setId(UNIT_ID_1);
        mockUnit1.setUnitName(UNIT_NAME_1);
        mockUnit1.setFloor(mockFloor);
        // Unit 2
        mockUnit2 = new Unit();
        mockUnit2.setId(UNIT_ID_2);
        mockUnit2.setUnitName("ROOM A102");
        mockUnit2.setFloor(mockFloor);


        // Utility Types
        mockWater = new Utility();
        mockWater.setUtilityName("water");
        mockElectric = new Utility();
        mockElectric.setUtilityName("electric");


        // Tenant (MOCK OBJECT) and Contract for Unit 1
        mockTenant1 = mock(User.class);

        // FIX for UnnecessaryStubbingException: Use lenient()
        lenient().when(mockTenant1.getFullName()).thenReturn("Alice Smith");

        mockContract1 = new Contract();
        mockContract1.setId(CONTRACT_ID_1);
        mockContract1.setUnit(mockUnit1);
        mockContract1.setTenant(mockTenant1);
        mockContract1.setStatus(Contract.ContractStatus.active);


        // Unit 1 Readings
        mockWaterReading1 = new UnitUtilities();
        mockWaterReading1.setId(WATER_UTIL_ID);
        mockWaterReading1.setUnit(mockUnit1);
        mockWaterReading1.setUtility(mockWater);
        mockWaterReading1.setUsageMonth(USAGE_MONTH);
        mockWaterReading1.setMeterStart(new BigDecimal("100.00"));
        mockWaterReading1.setMeterEnd(new BigDecimal("120.00"));
        mockWaterReading1.setCalculatedCost(new BigDecimal("500.00"));

        mockElectricReading1 = new UnitUtilities();
        mockElectricReading1.setId(ELECTRIC_UTIL_ID);
        mockElectricReading1.setUnit(mockUnit1);
        mockElectricReading1.setUtility(mockElectric);
        mockElectricReading1.setUsageMonth(USAGE_MONTH);
        mockElectricReading1.setMeterStart(new BigDecimal("2000.00"));
        mockElectricReading1.setMeterEnd(new BigDecimal("2100.00"));
        mockElectricReading1.setCalculatedCost(new BigDecimal("750.00"));
    }
}