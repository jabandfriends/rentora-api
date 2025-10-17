package com.rentora.api.MaintenanceTest;

import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.entity.Maintenance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GetMaintenanceMetadataTest extends MaintenanceServiceBaseTest {


    private final long TOTAL_COUNT = 50L;
    private final long COMPLETED_COUNT = 25L;
    private final long PENDING_COUNT = 15L;
    private final long IN_PROGRESS_COUNT = 5L;
    private final long URGENT_COUNT = 5L;


    @Test
    void test1_ShouldReturnCorrectCounts_WhenDataExists() {
        // Arrange

        when(maintenanceRepository.countMaintenanceByApartmentId(eq(APARTMENT_ID))).thenReturn(TOTAL_COUNT);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.completed), eq(APARTMENT_ID))).thenReturn(COMPLETED_COUNT);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.pending), eq(APARTMENT_ID))).thenReturn(PENDING_COUNT);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.in_progress), eq(APARTMENT_ID))).thenReturn(IN_PROGRESS_COUNT);
        when(maintenanceRepository.countMaintenanceByApartmentAndPriority(eq(APARTMENT_ID), eq(Maintenance.Priority.urgent))).thenReturn(URGENT_COUNT);

        // Act
        MaintenanceMetadataResponseDto result = maintenanceService.getMaintenanceMetadata(APARTMENT_ID);

        // Assert

        verify(maintenanceRepository, times(1)).countMaintenanceByApartmentId(eq(APARTMENT_ID));
        verify(maintenanceRepository, times(1)).countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.completed), eq(APARTMENT_ID));
        verify(maintenanceRepository, times(1)).countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.pending), eq(APARTMENT_ID));
        verify(maintenanceRepository, times(1)).countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.in_progress), eq(APARTMENT_ID));
        verify(maintenanceRepository, times(1)).countMaintenanceByApartmentAndPriority(eq(APARTMENT_ID), eq(Maintenance.Priority.urgent));


        assertEquals(TOTAL_COUNT, result.getTotalMaintenance());
        assertEquals(COMPLETED_COUNT, result.getCompletedCount());
        assertEquals(PENDING_COUNT, result.getPendingCount());
        assertEquals(IN_PROGRESS_COUNT, result.getInProgressCount());
        assertEquals(URGENT_COUNT, result.getUrgentCount());
    }

    @Test
    void test2_ShouldReturnZeroCounts_WhenNoDataExists() {
        // Arrange
        final long ZERO = 0L;

        when(maintenanceRepository.countMaintenanceByApartmentId(eq(APARTMENT_ID))).thenReturn(ZERO);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.completed), eq(APARTMENT_ID))).thenReturn(ZERO);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.pending), eq(APARTMENT_ID))).thenReturn(ZERO);
        when(maintenanceRepository.countMaintenanceByStatusAndApartmentId(eq(Maintenance.Status.in_progress), eq(APARTMENT_ID))).thenReturn(ZERO);
        when(maintenanceRepository.countMaintenanceByApartmentAndPriority(eq(APARTMENT_ID), eq(Maintenance.Priority.urgent))).thenReturn(ZERO);

        // Act
        MaintenanceMetadataResponseDto result = maintenanceService.getMaintenanceMetadata(APARTMENT_ID);

        // Assert
        assertEquals(ZERO, result.getTotalMaintenance());
        assertEquals(ZERO, result.getCompletedCount());
        assertEquals(ZERO, result.getPendingCount());
        assertEquals(ZERO, result.getInProgressCount());
        assertEquals(ZERO, result.getUrgentCount());
    }
}
