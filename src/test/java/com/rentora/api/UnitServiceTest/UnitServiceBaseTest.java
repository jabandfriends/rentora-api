package com.rentora.api.UnitServiceTest;

import com.rentora.api.model.entity.ApartmentService;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitServiceEntity;
import com.rentora.api.repository.ApartmentServiceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitServiceRepository;
import com.rentora.api.service.UnitServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public abstract class UnitServiceBaseTest {

    // 1. Dependencies
    @Mock
    protected UnitServiceRepository unitServiceRepository;
    @Mock
    protected UnitRepository unitRepository;
    @Mock
    protected ApartmentServiceRepository apartmentServiceRepository;

    // 2. Class Under Test
    @InjectMocks
    protected UnitServiceService unitServiceService;

    // 3. UUIDs
    protected final UUID UNIT_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    protected final UUID SERVICE_ID = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");
    protected final UUID UNIT_SERVICE_ID = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");
    protected final UUID NON_EXISTENT_ID = UUID.fromString("f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44");

    // 4. Entity Objects
    protected Unit mockUnit;
    protected ApartmentService mockService;
    protected UnitServiceEntity mockUnitServiceEntity;

    @BeforeEach
    void setUpBase() {
        // Mock Unit Entity
        mockUnit = new Unit();
        mockUnit.setId(UNIT_ID);
        mockUnit.setUnitName("A-101");

        // Mock Service Entity
        mockService = new ApartmentService();
        mockService.setId(SERVICE_ID);
        mockService.setServiceName("Internet Wi-Fi");
        mockService.setPrice(new BigDecimal("350.00"));

        // Mock UnitService Entity
        mockUnitServiceEntity = new UnitServiceEntity();
        mockUnitServiceEntity.setId(UNIT_SERVICE_ID);
        mockUnitServiceEntity.setUnit(mockUnit);
        mockUnitServiceEntity.setApartmentService(mockService);
        mockUnitServiceEntity.setMonthlyPrice(new BigDecimal("350.00"));
    }
}

