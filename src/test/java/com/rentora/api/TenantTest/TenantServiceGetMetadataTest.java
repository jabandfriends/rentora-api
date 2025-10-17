package com.rentora.api.TenantTest;

import com.rentora.api.model.dto.Tenant.Metadata.TenantsMetadataResponseDto;
import com.rentora.api.model.entity.Contract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TenantServiceGetMetadataTest extends TenantServiceBaseTest {

    // Mock constants for counter values
    private final Long TOTAL_TENANTS = 10L;
    private final Long TOTAL_ACTIVE = 7L;
    private final Long TOTAL_OCCUPIED = 5L;
    private final Long TOTAL_UNOCCUPIED = 2L; // Calculated as TOTAL_ACTIVE - TOTAL_OCCUPIED

    // Helper method to mock all counter repository calls
    private void mockRepositoryCounters(Long totalTenant, Long totalOccupied, Long totalActive, Long totalUnoccupied) {
        when(apartmentUserRepository.countByApartmentId(APARTMENT_ID)).thenReturn(totalTenant);
        when(userRepository.countByApartmentIdAndIsActiveTrueWithContractStatus(
                APARTMENT_ID, Contract.ContractStatus.active)).thenReturn(totalOccupied);
        when(apartmentUserRepository.countByApartmentIdAndIsActiveTrue(APARTMENT_ID)).thenReturn(totalActive);
        when(userRepository.countByApartmentIdAndIsActiveTrueWithFalseContractStatus(
                APARTMENT_ID, Contract.ContractStatus.active)).thenReturn(totalUnoccupied);
    }

    // ----------------------------------------------------------------------
    // 1. Happy Path: All Counters Return Positive Values
    // ----------------------------------------------------------------------
    @Test
    void getTenantsMetadata_ShouldReturnCorrectMetadata_WhenAllCountersArePositive() {
        // Arrange
        mockRepositoryCounters(TOTAL_TENANTS, TOTAL_OCCUPIED, TOTAL_ACTIVE, TOTAL_UNOCCUPIED);

        // Act
        TenantsMetadataResponseDto result = tenantService.getTenantsMetadata(APARTMENT_ID);

        // Assert
        assertEquals(TOTAL_TENANTS, result.getTotalTenants());
        assertEquals(TOTAL_ACTIVE, result.getTotalActiveTenants());
        assertEquals(TOTAL_OCCUPIED, result.getTotalOccupiedTenants());
        assertEquals(TOTAL_UNOCCUPIED, result.getTotalUnoccupiedTenants());

        // Verify all 4 repository calls were made
        verify(apartmentUserRepository).countByApartmentId(APARTMENT_ID);
        verify(userRepository).countByApartmentIdAndIsActiveTrueWithContractStatus(APARTMENT_ID, Contract.ContractStatus.active);
        verify(apartmentUserRepository).countByApartmentIdAndIsActiveTrue(APARTMENT_ID);
        verify(userRepository).countByApartmentIdAndIsActiveTrueWithFalseContractStatus(APARTMENT_ID, Contract.ContractStatus.active);
    }

    // ----------------------------------------------------------------------
    // 2. Edge Case: All Counters Return Zero
    // ----------------------------------------------------------------------
    @Test
    void getTenantsMetadata_ShouldReturnZeroes_WhenNoTenantsExist() {
        // Arrange
        Long ZERO = 0L;
        mockRepositoryCounters(ZERO, ZERO, ZERO, ZERO);

        // Act
        TenantsMetadataResponseDto result = tenantService.getTenantsMetadata(APARTMENT_ID);

        // Assert
        assertEquals(ZERO, result.getTotalTenants());
        assertEquals(ZERO, result.getTotalActiveTenants());
        assertEquals(ZERO, result.getTotalOccupiedTenants());
        assertEquals(ZERO, result.getTotalUnoccupiedTenants());
    }

    // ----------------------------------------------------------------------
    // 3. Edge Case: Active Tenants, but Zero Occupied/Unoccupied (Data Inconsistency Check)
    // ----------------------------------------------------------------------
    @Test
    void getTenantsMetadata_ShouldHandleZeroOccupiedAndUnoccupied_WhenActiveTenantsExist() {
        // Arrange
        Long TOTAL = 5L;
        Long ACTIVE = 5L;
        Long ZERO = 0L;

        // Mock: Total=5, Active=5, Occupied=0, Unoccupied=0
        mockRepositoryCounters(TOTAL, ZERO, ACTIVE, ZERO);

        // Act
        TenantsMetadataResponseDto result = tenantService.getTenantsMetadata(APARTMENT_ID);

        // Assert
        assertEquals(5L, result.getTotalTenants());
        assertEquals(5L, result.getTotalActiveTenants());
        assertEquals(0L, result.getTotalOccupiedTenants());
        assertEquals(0L, result.getTotalUnoccupiedTenants());
    }

    // ----------------------------------------------------------------------
    // 4. Edge Case: Only Active/Unoccupied Tenants Exist (High Unoccupied Rate)
    // ----------------------------------------------------------------------
    @Test
    void getTenantsMetadata_ShouldHandleHighUnoccupiedRate() {
        // Arrange
        Long TOTAL = 10L;
        Long ACTIVE = 8L;
        Long OCCUPIED = 2L;
        Long UNOCCUPIED = 6L;

        // Mock: Total=10, Active=8, Occupied=2, Unoccupied=6
        mockRepositoryCounters(TOTAL, OCCUPIED, ACTIVE, UNOCCUPIED);

        // Act
        TenantsMetadataResponseDto result = tenantService.getTenantsMetadata(APARTMENT_ID);

        // Assert
        assertEquals(TOTAL, result.getTotalTenants());
        assertEquals(ACTIVE, result.getTotalActiveTenants());
        assertEquals(OCCUPIED, result.getTotalOccupiedTenants());
        assertEquals(UNOCCUPIED, result.getTotalUnoccupiedTenants());
    }
}