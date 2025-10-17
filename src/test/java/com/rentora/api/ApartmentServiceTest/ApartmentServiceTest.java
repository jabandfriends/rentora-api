package com.rentora.api.ApartmentServiceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.model.entity.ServiceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApartmentServiceTest extends ApartmentServiceServiceBaseTest {

    @Test
    @DisplayName("GIVEN existing Apartment ID with services WHEN getApartmentService is called THEN return list of ServiceInfoDTO")
    void getApartmentService_Success_ReturnsDtoList() {
        // GIVEN
        List<ServiceEntity> mockServices = List.of(mockService1, mockService2);
        when(serviceRepository.findAllByApartmentId(APARTMENT_ID)).thenReturn(mockServices);

        // WHEN
        List<ServiceInfoDTO> result = apartmentServiceService.getApartmentService(APARTMENT_ID);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify mapping of the first service
        ServiceInfoDTO dto1 = result.get(0);
        assertEquals(SERVICE_ID_1, dto1.getId());
        assertEquals("Swimming Pool Access", dto1.getServiceName());
        // Use BigDecimal for accurate comparison
        assertEquals(new BigDecimal("500.00"), dto1.getPrice());

        // Verify mapping of the second service
        ServiceInfoDTO dto2 = result.get(1);
        assertEquals(SERVICE_ID_2, dto2.getId());
        assertEquals("High-speed Internet", dto2.getServiceName());
        // Use BigDecimal for accurate comparison
        assertEquals(new BigDecimal("799.50"), dto2.getPrice());

        verify(serviceRepository).findAllByApartmentId(APARTMENT_ID);
        verifyNoMoreInteractions(serviceRepository, unitRepository);
    }

    @Test
    @DisplayName("GIVEN Apartment ID has no services WHEN getApartmentService is called THEN throw ResourceNotFoundException")
    void getApartmentService_NoServicesFound_ThrowsResourceNotFound() {
        // GIVEN
        UUID apartmentId = NON_EXISTENT_ID;
        when(serviceRepository.findAllByApartmentId(apartmentId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> apartmentServiceService.getApartmentService(apartmentId));

        String expectedMessage = "No Services found for Apartment ID: " + apartmentId;
        assertEquals(expectedMessage, exception.getMessage());

        verify(serviceRepository).findAllByApartmentId(apartmentId);
        verifyNoInteractions(unitRepository);
    }
}

