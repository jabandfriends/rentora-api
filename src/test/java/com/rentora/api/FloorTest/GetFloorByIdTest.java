package com.rentora.api.FloorTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Response.FloorResponseRequestDto;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetFloorByIdTest extends FloorServiceBaseTest {

    @Test
    void test_GetFloorById_Success_ShouldReturnMappedDto() {
        // Arrange
        when(floorRepository.findById(FLOOR_ID)).thenReturn(Optional.of(mockFloor));

        // Act
        FloorResponseRequestDto result = floorService.getFloorById(FLOOR_ID);

        // Assert
        verify(floorRepository).findById(FLOOR_ID);

        // Assert DTO Content (Mapped from mockFloor)
        assertNotNull(result);
        assertEquals(FLOOR_ID, result.getFloorId());
        assertEquals(mockFloor.getFloorName(), result.getFloorName());
        assertEquals(mockBuilding.getName(), result.getBuildingName());
    }

    @Test
    void test_GetFloorById_FloorNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(floorRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            floorService.getFloorById(NON_EXISTENT_ID);
        });

        // Assert Exception Message
        assertEquals("Floor not found", exception.getMessage());
        verify(floorRepository).findById(NON_EXISTENT_ID);
    }
}
