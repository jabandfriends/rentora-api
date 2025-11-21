package com.rentora.api.FloorTest;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.Floor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateFloorByIdTest extends FloorServiceBaseTest {

    private void mockSuccessfulFinds() {
        when(mockUpdateRequest.getBuildingId()).thenReturn(BUILDING_ID);

        when(floorRepository.findById(FLOOR_ID)).thenReturn(Optional.of(mockFloor));
        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(mockBuilding));
    }

    @Test
    void testF1_Failure_FloorNotFound() {

        when(floorRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            floorService.updateFloorById(NON_EXISTENT_ID, mockUpdateRequest);
        });
        verify(buildingRepository, never()).findById(any());
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void testF2_Failure_BuildingNotFound() {
        when(mockUpdateRequest.getBuildingId()).thenReturn(BUILDING_ID);

        when(floorRepository.findById(FLOOR_ID)).thenReturn(Optional.of(mockFloor));
        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            floorService.updateFloorById(FLOOR_ID, mockUpdateRequest);
        });
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void testC1_Failure_ReduceTotalUnitsBelowCurrentUnits() {
        final long CURRENT_UNITS = 8L;
        final int REQUESTED_UNITS = 5;

        mockSuccessfulFinds();
        when(unitRepository.countByFloor(mockFloor)).thenReturn(CURRENT_UNITS);

        when(mockUpdateRequest.getTotalUnits()).thenReturn(REQUESTED_UNITS);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            floorService.updateFloorById(FLOOR_ID, mockUpdateRequest);
        });

        String expectedMessage = "Cannot reduce total units to 5 because there are already 8 unit(s) on this floor.";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void testC2_Failure_FloorNumberConflictWithAnotherFloor() {
        mockSuccessfulFinds();
        when(unitRepository.countByFloor(mockFloor)).thenReturn(0L);

        when(mockUpdateRequest.getFloorNumber()).thenReturn(2);

        when(floorRepository.findByBuildingAndFloorNumber(mockBuilding, 2))
                .thenReturn(Optional.of(mockAnotherFloor));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            floorService.updateFloorById(FLOOR_ID, mockUpdateRequest);
        });

        assertTrue(exception.getMessage().contains("Floor number 2 is already in use in this building."));
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void testC3_Success_FloorNumberIsSameAsCurrentFloor() {
        mockSuccessfulFinds();
        when(unitRepository.countByFloor(mockFloor)).thenReturn(0L);

        when(mockUpdateRequest.getFloorNumber()).thenReturn(1);

        when(floorRepository.findByBuildingAndFloorNumber(mockBuilding, 1))
                .thenReturn(Optional.of(mockFloor));

        floorService.updateFloorById(FLOOR_ID, mockUpdateRequest);

        verify(floorRepository, times(1)).save(mockFloor);
        verify(buildingRepository, times(1)).findById(mockUpdateRequest.getBuildingId());
    }

    @Test
    void testS1_Success_UpdateAllFields() {
        mockSuccessfulFinds();
        when(unitRepository.countByFloor(mockFloor)).thenReturn(0L);

        when(mockUpdateRequest.getFloorNumber()).thenReturn(1);
        when(mockUpdateRequest.getFloorName()).thenReturn("First Floor - Updated");
        when(mockUpdateRequest.getTotalUnits()).thenReturn(15);

        when(floorRepository.findByBuildingAndFloorNumber(mockBuilding, 1))
                .thenReturn(Optional.of(mockFloor));
        when(floorRepository.save(any(Floor.class))).thenAnswer(i -> i.getArguments()[0]);

        floorService.updateFloorById(FLOOR_ID, mockUpdateRequest);

        ArgumentCaptor<Floor> captor = ArgumentCaptor.forClass(Floor.class);
        verify(floorRepository).save(captor.capture());
        Floor capturedFloor = captor.getValue();

        assertEquals("First Floor - Updated", capturedFloor.getFloorName());
        assertEquals(1, capturedFloor.getFloorNumber());
        assertEquals(15, capturedFloor.getTotalUnits());
    }
}
