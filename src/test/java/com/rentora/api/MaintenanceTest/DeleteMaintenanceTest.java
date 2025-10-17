package com.rentora.api.MaintenanceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.Maintenance;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class DeleteMaintenanceTest extends MaintenanceServiceBaseTest {

    @Test
    void test1_Success_ShouldDeleteMaintenance() {
        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));

        maintenanceService.deleteMaintenance(MAINTENANCE_ID);

        verify(maintenanceRepository, times(1)).findById(MAINTENANCE_ID);
        verify(maintenanceRepository, times(1)).delete(mockPendingMaintenance);
    }

    @Test
    void test2_Failure_ShouldThrowResourceNotFoundException() {
        UUID nonExistentId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        when(maintenanceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.deleteMaintenance(nonExistentId));

        assertTrue(exception.getMessage().contains("Maintenance not found"));

        verify(maintenanceRepository, never()).delete(any(Maintenance.class));
    }
}
