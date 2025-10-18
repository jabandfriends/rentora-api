package com.rentora.api.UnitServiceTest;


import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.UnitService.Request.CreateUnitServiceRequest;
import com.rentora.api.model.dto.UnitService.Response.ExecuteUnitServiceResponse;
import com.rentora.api.model.entity.UnitServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateUnitServiceTest extends UnitServiceBaseTest{

    private CreateUnitServiceRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new CreateUnitServiceRequest();
        mockRequest.setServiceId(SERVICE_ID);
    }

    // --- TEST CASES FOR createUnitService() ---

    @Test
    @DisplayName("Should successfully create UnitService and return its ID")
    void createUnitService_shouldSucceedAndReturnId() {
        // Arrange
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(apartmentServiceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(mockService));
        when(unitServiceRepository.save(any(UnitServiceEntity.class))).thenReturn(mockUnitServiceEntity);

        // Act
        ExecuteUnitServiceResponse response = unitServiceService.createUnitService(UNIT_ID, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(UNIT_SERVICE_ID, response.getUnitServiceId());

        // Verify that the save operation was called exactly once
        verify(unitServiceRepository, times(1)).save(any(UnitServiceEntity.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when Unit ID is not found")
    void createUnitService_shouldThrowExceptionWhenUnitNotFound() {
        // Arrange
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitServiceService.createUnitService(UNIT_ID, mockRequest);
        });

        assertTrue(exception.getMessage().contains("Unit not found with ID: " + UNIT_ID));

        // Verify that the save operation was NEVER called
        verify(unitServiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when Service ID is not found")
    void createUnitService_shouldThrowExceptionWhenServiceNotFound() {
        // Arrange
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(apartmentServiceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitServiceService.createUnitService(UNIT_ID, mockRequest);
        });

        assertTrue(exception.getMessage().contains("Service not found with ID: " + SERVICE_ID));

        // Verify that the save operation was NEVER called
        verify(unitServiceRepository, never()).save(any());
    }
}
