package com.rentora.api.FloorTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Response.FloorResponseRequestDto;
import com.rentora.api.model.entity.Floor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetFloorByBuildingTest extends FloorServiceBaseTest {

    @Test
    void test_GetFloorByBuilding_Success_ReturnsListOfFloors() {
        // Arrange
        List<Floor> mockFloorList = List.of(mockFloor, mockAnotherFloor);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(mockBuilding));
        when(floorRepository.findByBuilding(eq(mockBuilding))).thenReturn(mockFloorList);

        // Act
        List<FloorResponseRequestDto> result = floorService.getFloorByBuilding(BUILDING_ID);

        // Assert (Verify behavior)
        verify(buildingRepository, times(1)).findById(BUILDING_ID);
        verify(floorRepository, times(1)).findByBuilding(eq(mockBuilding));

        // Assert (Verify result structure)
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void test_GetFloorByBuilding_BuildingNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(buildingRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            floorService.getFloorByBuilding(NON_EXISTENT_ID);
        });

        // Assert (Verify exception message)
        assertEquals("Building not found", exception.getMessage());

        // Verify (Check collaboration)
        verify(buildingRepository, times(1)).findById(NON_EXISTENT_ID);
        verify(floorRepository, never()).findByBuilding(any());
    }
}
