package com.rentora.api.UnitServiceTest;

import com.rentora.api.model.dto.UnitService.Request.CreateUnitServiceRequest;
import com.rentora.api.model.dto.UnitService.Response.UnitServiceInfoDTO;
import com.rentora.api.model.entity.ApartmentService;
import com.rentora.api.model.entity.UnitServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUnitServiceByUnitTest extends UnitServiceBaseTest {

    private CreateUnitServiceRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new CreateUnitServiceRequest();
        mockRequest.setServiceId(SERVICE_ID);
    }

    @Test
    @DisplayName("getUnitServicesByUnit return UnitServiceInfoDTO when UnitService Found")
    public void getUnitServicesByUnit() {
        UnitServiceEntity anotherUnitServiceEntity = new UnitServiceEntity();
        anotherUnitServiceEntity.setId(UUID.fromString("d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44"));
        anotherUnitServiceEntity.setUnit(mockUnit);

        ApartmentService mockService2 = new ApartmentService();
        mockService2.setServiceName("Cable TV");
        mockService2.setPrice(new BigDecimal("200.00"));
        anotherUnitServiceEntity.setApartmentService(mockService2);
        anotherUnitServiceEntity.setMonthlyPrice(new BigDecimal("200.00"));

        List<UnitServiceEntity> foundServices = Arrays.asList(
                mockUnitServiceEntity,
                anotherUnitServiceEntity
        );

        when(unitServiceRepository.findAllByUnitId(UNIT_ID)).thenReturn(foundServices);

        List<UnitServiceInfoDTO> result = unitServiceService.getUnitServicesByUnit(UNIT_ID);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(mockUnitServiceEntity.getId(), result.get(0).getId());
        assertEquals("A-101", result.get(0).getUnitName());
        assertEquals("Internet Wi-Fi", result.get(0).getServiceName());
        assertEquals(new BigDecimal("350.00"), result.get(0).getPrice());

        assertEquals("Cable TV", result.get(1).getServiceName());

        verify(unitServiceRepository, times(1)).findAllByUnitId(UNIT_ID);
    }

    @Test
    @DisplayName("getUnitServicesByUnit return Empty list when UnitService not Found")
    public void getUnitServicesByUnitNotFound() {
        when(unitServiceRepository.findAllByUnitId(UNIT_ID)).thenReturn(Collections.emptyList());

        List<UnitServiceInfoDTO> result = unitServiceService.getUnitServicesByUnit(UNIT_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(unitServiceRepository, times(1)).findAllByUnitId(UNIT_ID);
    }
}
