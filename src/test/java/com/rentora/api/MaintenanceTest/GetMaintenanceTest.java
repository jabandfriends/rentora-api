package com.rentora.api.MaintenanceTest;

import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.entity.Maintenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetMaintenanceTest extends MaintenanceServiceBaseTest {

    private final Pageable defaultPageable = PageRequest.of(0, 10);
    private Page<Maintenance> mockMaintenancePage; // แก้ไข: ลบ final และยกเลิกการเริ่มต้นค่า ณ จุดนี้

    @BeforeEach
    void setUpGetTest() {
        // แก้ไข: ย้าย Logic การสร้าง Page มาไว้ใน @BeforeEach เพื่อให้มั่นใจว่า mockPendingMaintenance ถูกตั้งค่าแล้ว
        this.mockMaintenancePage = new PageImpl<>(List.of(mockPendingMaintenance));
    }

    @Test
    void test1_SearchWithNoFilters_ShouldCallFindAll() {
        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(mockMaintenancePage);

        maintenanceService.getMaintenance(null, null, null, null, null, null, defaultPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) any(), eq(defaultPageable));
    }

    @Test
    void test2_ComplexFilter_ShouldCallFindAllWithSpecification() {
        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(mockMaintenancePage);

        maintenanceService.getMaintenance(APARTMENT_ID, "faucet", null, false, UNIT_ID, Maintenance.Priority.normal, defaultPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) argThat(spec -> spec != null), eq(defaultPageable));
    }

    @Test
    void test3_ConditionalStatusFilter_ShouldApplyStatus() {
        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(mockMaintenancePage);

        maintenanceService.getMaintenance(APARTMENT_ID, null, Maintenance.Status.pending, null, null, null, defaultPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) argThat(spec -> spec != null), eq(defaultPageable));
    }

    @Test
    void test4_NameFilterOnly_ShouldCallFindAllWithSpecification() {
        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(mockMaintenancePage);

        maintenanceService.getMaintenance(null, "leaking", null, null, null, null, defaultPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) argThat(spec -> spec != null), eq(defaultPageable));
    }

    @Test
    void test5_EmptyResult_ShouldReturnEmptyPage() {
        Page<Maintenance> emptyPage = new PageImpl<>(List.of());

        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(emptyPage);

        Page<MaintenanceInfoDTO> result = maintenanceService.getMaintenance(APARTMENT_ID, null, null, null, null, null, defaultPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) any(), eq(defaultPageable));
        assertTrue(result.isEmpty());
    }

    @Test
    void test6_MappingVerification_ShouldReturnDTOWithCorrectBuildingAndUnitNames() {
        // ตั้งค่าเพิ่มเติมสำหรับ DTO mapping ใน test นี้โดยเฉพาะ
        mockPendingMaintenance.getUnit().getFloor().getBuilding().setName("Alpha Building");
        mockPendingMaintenance.getUnit().setUnitName("Unit-A");

        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(defaultPageable))).thenReturn(mockMaintenancePage);

        Page<MaintenanceInfoDTO> result = maintenanceService.getMaintenance(null, null, null, null, null, null, defaultPageable);

        assertFalse(result.isEmpty());
        MaintenanceInfoDTO dto = result.getContent().get(0);

        assertEquals(MAINTENANCE_ID, dto.getId());
        assertEquals("Alpha Building", dto.getBuildingsName());
        assertEquals("Unit-A", dto.getUnitName());
        assertEquals(Maintenance.Status.pending, dto.getStatus());
    }

    @Test
    void test7_PageableVerification_ShouldPassCorrectPageableToRepository() {
        Pageable customPageable = PageRequest.of(1, 20, Sort.by("id").descending());
        Page<Maintenance> emptyPage = new PageImpl<>(List.of());

        when(maintenanceRepository.findAll((Specification<Maintenance>) any(), eq(customPageable))).thenReturn(emptyPage);

        maintenanceService.getMaintenance(null, null, null, null, null, null, customPageable);

        verify(maintenanceRepository).findAll((Specification<Maintenance>) any(), eq(customPageable));
    }
}
