package com.rentora.api.FloorTest;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.model.entity.Floor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateFloorTest extends FloorServiceBaseTest {

    @Test
    @DisplayName("GIVEN valid request and building has capacity WHEN createFloor is called THEN floor is created and ID is returned")
    void createFloor_Success() {
        // FIX: Stub the getter of the mock DTO to return the correct ID
        when(mockCreateRequest.getBuildingId()).thenReturn(BUILDING_ID);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(mockBuilding));
        when(floorRepository.countByBuilding(mockBuilding)).thenReturn(1L);
        when(floorRepository.findByBuildingAndFloorNumber(mockBuilding, mockCreateRequest.getFloorNumber()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<Floor> floorCaptor = ArgumentCaptor.forClass(Floor.class);
        when(floorRepository.save(floorCaptor.capture())).thenAnswer(invocation -> {
            Floor savedFloor = invocation.getArgument(0);
            savedFloor.setId(FLOOR_ID);
            return savedFloor;
        });

        CreateFloorResponseDto response = floorService.createFloor(mockCreateRequest);

        assertNotNull(response);
        assertEquals(FLOOR_ID, response.getId());

        verify(buildingRepository).findById(BUILDING_ID);
        verify(floorRepository).countByBuilding(mockBuilding);
        verify(floorRepository).findByBuildingAndFloorNumber(mockBuilding, mockCreateRequest.getFloorNumber());

        Floor savedFloor = floorCaptor.getValue();
        assertEquals(mockBuilding, savedFloor.getBuilding());
        assertEquals(mockCreateRequest.getFloorName(), savedFloor.getFloorName());
        assertEquals(mockCreateRequest.getFloorNumber(), savedFloor.getFloorNumber());
        assertEquals(mockCreateRequest.getTotalUnits(), savedFloor.getTotalUnits());

        verify(floorRepository).save(any(Floor.class));
        verifyNoMoreInteractions(buildingRepository, floorRepository, unitRepository);
    }

    @Test
    @DisplayName("GIVEN non-existent Building ID WHEN createFloor is called THEN ResourceNotFoundException is thrown")
    void createFloor_BuildingNotFound_ThrowsResourceNotFound() {
        // FIX: Remove incorrect setter call and stub the getter instead
        when(mockCreateRequest.getBuildingId()).thenReturn(NON_EXISTENT_ID);

        when(buildingRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> floorService.createFloor(mockCreateRequest),
                "Building not found");

        verify(buildingRepository).findById(NON_EXISTENT_ID);
        verifyNoInteractions(floorRepository, unitRepository);
    }

    @Test
    @DisplayName("GIVEN building is at max capacity WHEN createFloor is called THEN BadRequestException is thrown")
    void createFloor_MaxCapacity_ThrowsBadRequest() {
        // FIX: Stub the getter of the mock DTO to return the correct ID
        when(mockCreateRequest.getBuildingId()).thenReturn(BUILDING_ID);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(mockBuilding));
        when(floorRepository.countByBuilding(mockBuilding)).thenReturn((long) mockBuilding.getTotalFloors());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> floorService.createFloor(mockCreateRequest));

        String expectedMessage = "Cannot create a new floor. This building already has " + mockBuilding.getTotalFloors() + " floor(s), which is the maximum allowed.";
        assertEquals(expectedMessage, exception.getMessage());

        verify(buildingRepository).findById(BUILDING_ID);
        verify(floorRepository).countByBuilding(mockBuilding);
        verifyNoMoreInteractions(buildingRepository, floorRepository, unitRepository);
    }

    @Test
    @DisplayName("GIVEN floor number is already in use in the building WHEN createFloor is called THEN BadRequestException is thrown")
    void createFloor_FloorNumberAlreadyInUse_ThrowsBadRequest() {
        when(mockCreateRequest.getBuildingId()).thenReturn(BUILDING_ID);

        int duplicateFloorNumber = mockFloor.getFloorNumber(); // Value is 1

        // STUB: Ensure the service calls getFloorNumber() and gets the duplicate value (1)
        when(mockCreateRequest.getFloorNumber()).thenReturn(duplicateFloorNumber);

        when(buildingRepository.findById(BUILDING_ID)).thenReturn(Optional.of(mockBuilding));
        when(floorRepository.countByBuilding(mockBuilding)).thenReturn(1L);
        // Stub the repository call with the correct duplicate number (1)
        when(floorRepository.findByBuildingAndFloorNumber(eq(mockBuilding), eq(duplicateFloorNumber)))
                .thenReturn(Optional.of(mockFloor));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> floorService.createFloor(mockCreateRequest));

        String expectedMessage = "Cannot create a new floor. This floor number is already in use. ";
        assertEquals(expectedMessage, exception.getMessage());

        verify(buildingRepository).findById(BUILDING_ID);
        verify(floorRepository).countByBuilding(mockBuilding);
        // Verify the repository was queried with the correct duplicate floor number (1)
        verify(floorRepository).findByBuildingAndFloorNumber(eq(mockBuilding), eq(duplicateFloorNumber));
        verifyNoMoreInteractions(buildingRepository, floorRepository, unitRepository);
    }
}
