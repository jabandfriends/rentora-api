package com.rentora.api.MaintenanceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetMaintenanceByIdTest extends MaintenanceServiceBaseTest {

    @Test
    void test1_Success_ShouldReturnCorrectDTO() {
        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));

        MaintenanceDetailDTO result = maintenanceService.getMaintenanceById(MAINTENANCE_ID);

        verify(maintenanceRepository, times(1)).findById(MAINTENANCE_ID);
        assertNotNull(result);
        assertEquals(MAINTENANCE_ID, result.getId());
        assertEquals(mockPendingMaintenance.getTitle(), result.getTitle());
        assertEquals(mockPendingMaintenance.getStatus(), result.getStatus());
        assertEquals(mockPendingMaintenance.getPriority(), result.getPriority());

        assertEquals(mockPendingMaintenance.getUnit().getFloor().getBuilding().getName(), result.getBuildingsName());
        assertEquals(mockPendingMaintenance.getUnit().getUnitName(), result.getUnitName());
    }

    @Test
    void test2_Failure_ShouldThrowResourceNotFoundException() {
        UUID nonExistentId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        when(maintenanceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.getMaintenanceById(nonExistentId));

        assertTrue(exception.getMessage().contains("Maintenance not found"));

        verify(maintenanceRepository, times(1)).findById(nonExistentId);
    }
}
