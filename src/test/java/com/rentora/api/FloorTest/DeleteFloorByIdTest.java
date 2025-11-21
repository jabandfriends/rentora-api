package com.rentora.api.FloorTest;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.Floor;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteFloorByIdTest extends FloorServiceBaseTest {

    @Test
    void test1_Success_ShouldDeleteFloor_WhenUnitCountIsZero() {
        final long NO_UNITS = 0L;
        when(floorRepository.findById(FLOOR_ID)).thenReturn(Optional.of(mockFloor));
        when(unitRepository.countByFloor(mockFloor)).thenReturn(NO_UNITS);

        floorService.deleteFloorById(FLOOR_ID);

        verify(floorRepository, times(1)).findById(FLOOR_ID);
        verify(unitRepository, times(1)).countByFloor(mockFloor);
        verify(floorRepository, times(1)).deleteById(FLOOR_ID);
    }

    @Test
    void test2_Failure_ShouldThrowResourceNotFoundException_WhenFloorNotFound() {
        when(floorRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            floorService.deleteFloorById(NON_EXISTENT_ID);
        });

        assertEquals("Floor not found", exception.getMessage());
        verify(unitRepository, never()).countByFloor(any(Floor.class));
        verify(floorRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void test3_Failure_ShouldThrowBadRequestException_WhenUnitsAreAssigned() {
        final long HAS_UNITS = 5L;
        when(floorRepository.findById(FLOOR_ID)).thenReturn(Optional.of(mockFloor));
        when(unitRepository.countByFloor(mockFloor)).thenReturn(HAS_UNITS);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            floorService.deleteFloorById(FLOOR_ID);
        });

        String expectedMessage = "This floor cannot be deleted because it has 5 unit(s) assigned to it.";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(floorRepository, never()).deleteById(any(UUID.class));
    }
}
