package com.rentora.api.MaintenanceTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Unit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateMaintenanceTest extends MaintenanceServiceBaseTest {

    private Unit createUnitWithActiveContract() {

        Unit unitWithContract = mockUnit;
        unitWithContract.setContracts(List.of(mockActiveContract));
        return unitWithContract;
    }

    @Test
    void test1_Success_TenantSetFromActiveContract() {
        // Arrange
        Unit unitWithContract = createUnitWithActiveContract();
        CreateMaintenanceRequest request = new CreateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setTitle("Drainage Issue");
        request.setPriority(Maintenance.Priority.high);

        Maintenance savedMaintenance = new Maintenance();
        savedMaintenance.setId(MAINTENANCE_ID);

        // Mock: Unit with Contract
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithContract));
        // Mock: Repository save return Maintenance
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(savedMaintenance);

        // Act
        ExecuteMaintenanceResponse result = maintenanceService.createMaintenance(TENANT_USER_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();

        assertNotNull(result);
        assertEquals(MAINTENANCE_ID, result.getMaintenanceId());
        assertNotNull(captured.getTenantUser());
        assertEquals(TENANT_USER_ID, captured.getTenantUser().getId());

        assertEquals("Drainage Issue", captured.getTitle());
        assertEquals(LocalDate.now(), captured.getRequestedDate());
        assertEquals(unitWithContract, captured.getUnit());
    }

    @Test
    void test2_Success_NoActiveTenantFound() {

        // Arrange
        mockUnit.setContracts(Collections.emptyList()); // Base class setContracts(Collections.emptyList());
        CreateMaintenanceRequest request = new CreateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setTitle("Light bulb broken");

        Maintenance savedMaintenance = new Maintenance();
        savedMaintenance.setId(MAINTENANCE_ID);

        // Mock: Unit
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(mockUnit));
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(savedMaintenance);

        // Act
        ExecuteMaintenanceResponse result = maintenanceService.createMaintenance(TENANT_USER_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());

        Maintenance captured = captor.getValue();


        assertNull(captured.getTenantUser());
        assertEquals(MAINTENANCE_ID, result.getMaintenanceId());
    }

    @Test
    void test3_Failure_UnitNotFound() {
        // Arrange
        CreateMaintenanceRequest request = new CreateMaintenanceRequest();
        request.setUnitId(UNIT_ID);

        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createMaintenance(TENANT_USER_ID, request));

        assertTrue(exception.getMessage().contains("Unit not found with ID"));

        verify(maintenanceRepository, never()).save(any(Maintenance.class));
    }

    @Test
    void test4_Success_FullMappingVerification() {
        // Arrange
        Unit unitWithContract = createUnitWithActiveContract();
        CreateMaintenanceRequest request = new CreateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setTitle("Full Check");
        request.setDescription("All fields set");
        request.setCategory(Maintenance.Category.electrical);
        request.setPriority(Maintenance.Priority.urgent);
        request.setAppointmentDate(OffsetDateTime.now().plusDays(1));
        request.setDueDate(OffsetDateTime.now().plusDays(5));
        request.setEstimatedHours(new BigDecimal("4.5"));
        request.setEstimatedCost(new BigDecimal("3500.00"));
        request.setIsEmergency(true);
        request.setIsRecurring(true);
        request.setRecurringSchedule(Maintenance.RecurringSchedule.monthly);
        request.setStatus(Maintenance.Status.in_progress);

        Maintenance savedMaintenance = new Maintenance();
        savedMaintenance.setId(MAINTENANCE_ID);

        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithContract));
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(savedMaintenance);

        // Act
        maintenanceService.createMaintenance(TENANT_USER_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());
        Maintenance captured = captor.getValue();

        assertEquals("Full Check", captured.getTitle());
        assertEquals(new BigDecimal("4.5"), captured.getEstimatedHours());
        assertTrue(captured.getIsEmergency());
        assertTrue(captured.getIsRecurring());

        assertEquals(Maintenance.RecurringSchedule.monthly, captured.getRecurringSchedule());
        assertEquals(Maintenance.Status.in_progress, captured.getStatus());
    }

    @Test
    void test5_Success_MappingWhenOptionalFieldsAreNull() {
        // Arrange
        Unit unitWithContract = createUnitWithActiveContract();
        CreateMaintenanceRequest request = new CreateMaintenanceRequest();
        request.setUnitId(UNIT_ID);
        request.setTitle("Minimal Request");

        Maintenance savedMaintenance = new Maintenance();
        savedMaintenance.setId(MAINTENANCE_ID);

        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithContract));
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(savedMaintenance);

        // Act
        maintenanceService.createMaintenance(TENANT_USER_ID, request);

        // Assert
        ArgumentCaptor<Maintenance> captor = ArgumentCaptor.forClass(Maintenance.class);
        verify(maintenanceRepository).save(captor.capture());
        Maintenance captured = captor.getValue();

        assertNull(captured.getRecurringSchedule());

        assertNull(captured.getEstimatedHours());
        assertNull(captured.getAppointmentDate());
    }
}
