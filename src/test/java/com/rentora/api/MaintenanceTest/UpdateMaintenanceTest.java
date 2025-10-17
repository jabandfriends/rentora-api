package com.rentora.api.MaintenanceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.entity.Maintenance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UpdateMaintenanceTest extends MaintenanceServiceBaseTest {

    private final OffsetDateTime FIXED_START_DATE = OffsetDateTime.of(2025, 10, 15, 10, 0, 0, 0, ZoneOffset.UTC);

    // --- I. FAILURE & BASE CASES ---

    @Test
    void testF1_Failure_MaintenanceNotFound() {
        // Arrange
        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.updateMaintenance(MAINTENANCE_ID, request));
        verify(unitRepository, never()).findById(any());
        verify(maintenanceRepository, never()).save(any());
    }

    @Test
    void testF2_Failure_UnitNotFound() {
        // Arrange
        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.updateMaintenance(MAINTENANCE_ID, request));
        verify(maintenanceRepository, never()).save(any());
    }

    @Test
    void testB1_BaseUpdate_ShouldUpdateTitleAndDescriptionOnly() {
        // Arrange
        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setTitle("New Title Updated");
        request.setDescription("New Description Updated");
        request.setUnitId(UNIT_ID);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ExecuteMaintenanceResponse result = maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(MAINTENANCE_ID, result.getMaintenanceId());
        assertEquals("New Title Updated", captured.getTitle());
        assertEquals("New Description Updated", captured.getDescription());

        assertEquals(Maintenance.Status.pending, captured.getStatus());
    }

    // --- II. CONDITIONAL FIELD UPDATES ---

    @Test
    void testC1_UpdateStatusOnly_ShouldUpdateStatusWithoutRecurLogic() {
        // Arrange
        // Mock Maintenance that not Recurring
        mockPendingMaintenance.setIsRecurring(false);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE);

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setStatus(Maintenance.Status.in_progress); // อัปเดตสถานะเท่านั้น

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.in_progress, captured.getStatus());
        assertFalse(captured.getIsRecurring());
        assertEquals(FIXED_START_DATE, captured.getAppointmentDate());
    }

    @Test
    void testC2_UpdateRecurringToFalse_ShouldSetRecurringScheduleToNull() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.monthly);

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(false);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();
        // if(request.getIsRecurring() != null && !request.getIsRecurring()) { maintenance.setRecurringSchedule(null); }
        assertFalse(captured.getIsRecurring());
        assertNull(captured.getRecurringSchedule());
    }

    @Test
    void testC3_UpdateAllOptionalFields_ShouldMapCorrectly() {
        // Arrange
        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setEstimatedHours(new BigDecimal("8.0"));
        request.setEstimatedCost(new BigDecimal("5000.00"));
        request.setIsEmergency(true);
        request.setAppointmentDate(FIXED_START_DATE.plusDays(1));

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(new BigDecimal("8.0"), captured.getEstimatedHours());
        assertEquals(new BigDecimal("5000.00"), captured.getEstimatedCost());
        assertTrue(captured.getIsEmergency());
        assertEquals(FIXED_START_DATE.plusDays(1), captured.getAppointmentDate());
    }

    // --- III. RECURRING LOGIC ---

    @Test
    void testR2_RecurringSkip_WhenNotRecurringButCompleted() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(false); // เป็น false

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setStatus(Maintenance.Status.completed); // แต่ Request ส่ง Completed มา

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        // Recurring Logic
        assertEquals(Maintenance.Status.completed, captured.getStatus());
        assertFalse(captured.getIsRecurring());
        assertNull(captured.getRecurringSchedule());
    }

    // --- WEEKLY (R3) ---
    @Test
    void testR3_Success_RecurringWeeklyCompletion() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.weekly);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE); // 2025-10-15

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(true);
        request.setStatus(Maintenance.Status.completed); // Trigger Recurring Logic

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.pending, captured.getStatus());
        OffsetDateTime expectedNextDate = FIXED_START_DATE.plusWeeks(1); // 2025-10-22
        assertEquals(expectedNextDate, captured.getAppointmentDate());
    }

    // --- MONTHLY (R4) ---
    @Test
    void testR4_Success_RecurringMonthlyCompletion() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.monthly);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE); // 2025-10-15

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(true);
        request.setStatus(Maintenance.Status.completed);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.pending, captured.getStatus());
        OffsetDateTime expectedNextDate = FIXED_START_DATE.plusMonths(1); // 2025-11-15
        assertEquals(expectedNextDate, captured.getAppointmentDate());
    }

    // --- QUARTERLY (R5) ---
    @Test
    void testR5_Success_RecurringQuarterlyCompletion() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.quarterly);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE); // 2025-10-15

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(true);
        request.setStatus(Maintenance.Status.completed);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.pending, captured.getStatus());
        OffsetDateTime expectedNextDate = FIXED_START_DATE.plusMonths(3); // 2026-01-15
        assertEquals(expectedNextDate, captured.getAppointmentDate());
    }

    // --- YEARLY (R6) ---
    @Test
    void testR6_Success_RecurringYearlyCompletion() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.yearly);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE); // 2025-10-15

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(true);
        request.setStatus(Maintenance.Status.completed);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.pending, captured.getStatus());
        OffsetDateTime expectedNextDate = FIXED_START_DATE.plusYears(1); // 2026-10-15
        assertEquals(expectedNextDate, captured.getAppointmentDate());
    }

    // --- RECURRING SKIP (R1) ---
    @Test
    void testR1_RecurringSkip_WhenRecurringButNotInProgressStatus() {
        // Arrange
        mockPendingMaintenance.setIsRecurring(true);
        mockPendingMaintenance.setRecurringSchedule(Maintenance.RecurringSchedule.weekly);
        mockPendingMaintenance.setAppointmentDate(FIXED_START_DATE);
        mockPendingMaintenance.setStatus(Maintenance.Status.in_progress);

        UpdateMaintenanceRequest request = new UpdateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setIsRecurring(true);
        request.setStatus(Maintenance.Status.in_progress);

        when(maintenanceRepository.findById(MAINTENANCE_ID)).thenReturn(Optional.of(mockPendingMaintenance));
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        maintenanceService.updateMaintenance(MAINTENANCE_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertEquals(Maintenance.Status.in_progress, captured.getStatus());
        assertEquals(FIXED_START_DATE, captured.getAppointmentDate());
        assertEquals(Maintenance.RecurringSchedule.weekly, captured.getRecurringSchedule());
    }
}
