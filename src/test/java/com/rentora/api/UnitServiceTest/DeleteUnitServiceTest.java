package com.rentora.api.UnitServiceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.UnitService.Request.CreateUnitServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class DeleteUnitServiceTest extends UnitServiceBaseTest {

    private CreateUnitServiceRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new CreateUnitServiceRequest();
        mockRequest.setServiceId(SERVICE_ID);
    }

    // --- TEST CASES FOR deleteUnitService() ---

    @Test
    @DisplayName("Should successfully delete UnitService when ID is found")
    void deleteUnitService_shouldSucceedWhenFound() {
        // Arrange
        when(unitServiceRepository.findById(UNIT_SERVICE_ID)).thenReturn(Optional.of(mockUnitServiceEntity));

        // Act
        unitServiceService.deleteUnitService(UNIT_SERVICE_ID);

        // Assert
        verify(unitServiceRepository, times(1)).delete(mockUnitServiceEntity);
        verify(unitServiceRepository, times(1)).findById(UNIT_SERVICE_ID);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when UnitService ID is not found")
    void deleteUnitService_shouldThrowExceptionWhenNotFound() {
        // Arrange
        when(unitServiceRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitServiceService.deleteUnitService(NON_EXISTENT_ID);
        });

        assertTrue(exception.getMessage().contains("Service not found with ID: "));

        // Verify that the delete operation was NEVER called
        verify(unitServiceRepository, never()).delete(any());
    }
}

