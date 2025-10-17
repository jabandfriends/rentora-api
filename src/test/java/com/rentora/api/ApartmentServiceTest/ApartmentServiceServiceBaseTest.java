package com.rentora.api.ApartmentServiceTest;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ServiceEntity;
import com.rentora.api.repository.ApartmentServiceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.service.ApartmentServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public abstract class ApartmentServiceServiceBaseTest {

    @Mock
    protected ApartmentServiceRepository serviceRepository;
    @Mock
    protected UnitRepository unitRepository;

    @InjectMocks
    protected ApartmentServiceService apartmentServiceService;

    protected final UUID APARTMENT_ID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    protected final UUID SERVICE_ID_1 = UUID.fromString("50000000-0000-0000-0000-000000000001");
    protected final UUID SERVICE_ID_2 = UUID.fromString("50000000-0000-0000-0000-000000000002");
    protected final UUID NON_EXISTENT_ID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

    protected Apartment mockApartment;
    protected ServiceEntity mockService1;
    protected ServiceEntity mockService2;

    @BeforeEach
    void setUpBase() {
        mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);

        mockService1 = new ServiceEntity();
        mockService1.setId(SERVICE_ID_1);
        mockService1.setServiceName("Swimming Pool Access");
        mockService1.setPrice(new BigDecimal("500.00"));

        mockService2 = new ServiceEntity();
        mockService2.setId(SERVICE_ID_2);
        mockService2.setServiceName("High-speed Internet");
        mockService2.setPrice(new BigDecimal ("799.50"));
    }
}

