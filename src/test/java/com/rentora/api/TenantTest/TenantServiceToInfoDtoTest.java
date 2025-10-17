package com.rentora.api.TenantTest;

import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public class TenantServiceToInfoDtoTest extends TenantServiceBaseTest {

    // ----------------------------------------------------------------------
    // 1. Happy Path: Occupied Tenant (Active Contract)
    // ----------------------------------------------------------------------
    @Test
    void toTenantInfoDto_ShouldSetOccupiedTrueAndUnitName_WhenActiveContractExists() {
        // Arrange
        // mockApartmentUserOccupied ถูกตั้งค่าให้มี active contract และ unit name ใน Base Test แล้ว

        // Act
        TenantInfoDto result = tenantService.toTenantInfoDto(mockApartmentUserOccupied);

        // Assert
        // Basic Info
        assertEquals(USER_NAME_ACTIVE, result.getFullName());
        assertEquals("john.doe@test.com", result.getEmail());
        assertEquals(USER_ID_OCCUPIED, result.getUserId());
        assertEquals(APARTMENT_USER_ID_OCCUPIED, result.getApartmentUserId());
        assertTrue(result.isAccountStatus()); // IsActive = true

        // Key Logic: Occupied Status and Unit Name
        assertTrue(result.isAccountStatus());
        assertEquals(UNIT_NAME_1, result.getUnitName());
        assertNotNull(result.getCreatedAt());
    }

    // ----------------------------------------------------------------------
    // 2. Case: Unoccupied Tenant (No Active Contract)
    // ----------------------------------------------------------------------
    @Test
    void toTenantInfoDto_ShouldSetOccupiedFalseAndNullUnitName_WhenNoActiveContractExists() {
        // Arrange
        // mockApartmentUserUnoccupied ถูกตั้งค่าให้มี Contracts เป็น Empty List ใน Base Test แล้ว

        // Act
        TenantInfoDto result = tenantService.toTenantInfoDto(mockApartmentUserUnoccupied);

        // Assert
        // Basic Info (should be active account, but unoccupied)
        assertTrue(result.isAccountStatus()); // IsActive = true

        // Key Logic: Occupied Status and Unit Name
        assertFalse(result.isOccupiedStatus());
        assertNull(result.getUnitName());
    }

    // ----------------------------------------------------------------------
    // 3. Edge Case: Multiple Contracts (One Active, Others Inactive)
    // ----------------------------------------------------------------------
    @Test
    void toTenantInfoDto_ShouldUseFirstActiveContract_WhenMultipleContractsExist() {
        // Arrange
        // 1. Inactive Contract with a different unit
        Contract mockInactiveContract = mock(Contract.class);
        lenient().when(mockInactiveContract.getStatus()).thenReturn(Contract.ContractStatus.terminated);

        // 2. Second Active Contract (Should be ignored as only the first is picked)
        Contract mockSecondActiveContract = mock(Contract.class);
        lenient().when(mockSecondActiveContract.getStatus()).thenReturn(Contract.ContractStatus.active);

        // FIXED: Create a new Mock Unit and Stubbing getUnit()
        String SECOND_UNIT_NAME = "ROOM B202";
        Unit mockSecondUnit = mock(Unit.class); // <-- NEW MOCK UNIT
        lenient().when(mockSecondUnit.getUnitName()).thenReturn(SECOND_UNIT_NAME);
        lenient().when(mockSecondActiveContract.getUnit()).thenReturn(mockSecondUnit); // <-- FIXED NPE SOURCE

        // Setup User Spy to return a list of contracts where the original 'mockActiveContract' is first
        List<Contract> mixedContracts = List.of(
                mockActiveContract, // Active contract with UNIT_NAME_1
                mockInactiveContract,
                mockSecondActiveContract
        );
        lenient().when(mockUserOccupied.getContracts()).thenReturn(mixedContracts);

        // Act
        TenantInfoDto result = tenantService.toTenantInfoDto(mockApartmentUserOccupied);

        // Assert
        assertTrue(result.isOccupiedStatus());
        // Verify that the UnitName is taken from the FIRST active contract (UNIT_NAME_1)
        assertEquals(UNIT_NAME_1, result.getUnitName());
    }
}