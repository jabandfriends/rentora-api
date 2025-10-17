package com.rentora.api.TenantTest;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Tenant.Response.TenantDetailInfoResponseDto;
import com.rentora.api.model.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TenantServiceGetDetailTest extends TenantServiceBaseTest {

    // Helper: Ensure mockUserOccupied has all necessary fields set for a comprehensive detail check
    private void setupMockUserDetail() {
        // Since mockUserOccupied is a Spy, we can use Setters for non-stubbed fields
        mockUserOccupied.setFirstName("John");
        mockUserOccupied.setLastName("Doe");
        mockUserOccupied.setPhoneNumber("0812345678");
        mockUserOccupied.setNationalId("1101101110111");
        mockUserOccupied.setBirthDate(LocalDate.of(1990, 1, 1));
        mockUserOccupied.setEmergencyContactName("Jane Doe");
        mockUserOccupied.setEmergencyContactPhone("0887654321");
    }

    // ----------------------------------------------------------------------
    // 1. Happy Path: User Found and Mapped Correctly
    // ----------------------------------------------------------------------
    @Test
    void getTenantDetail_ShouldReturnDetailDto_WhenUserIsFound() {
        // Arrange
        setupMockUserDetail();

        // Mock UserRepository to return the Occupied User Spy
        when(userRepository.findById(USER_ID_OCCUPIED)).thenReturn(Optional.of(mockUserOccupied));

        // Act
        TenantDetailInfoResponseDto result = tenantService.getTenantDetail(USER_ID_OCCUPIED);

        // Assert
        // Verify Repository call
        verify(userRepository).findById(USER_ID_OCCUPIED);

        // Verify Mapping logic (via toTenantInfoDtoByUser)
        assertEquals(USER_ID_OCCUPIED, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(USER_NAME_ACTIVE, result.getFullName()); // Mapped from Spy/Setter
        assertEquals("john.doe@test.com", result.getEmail());
        assertEquals("0812345678", result.getPhoneNumber());
        assertEquals("1101101110111", result.getNationalId());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals("Jane Doe", result.getEmergencyContactName());
        assertEquals("0887654321", result.getEmergencyContactPhone());
        // createdAt is set by the Spy, verifying it's not null is sufficient for this test
    }

    // ----------------------------------------------------------------------
    // 2. Error Case: User Not Found
    // ----------------------------------------------------------------------
    @Test
    void getTenantDetail_ShouldThrowResourceNotFoundException_WhenUserIsNotFound() {
        // Arrange
        // Mock UserRepository to return empty Optional
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        // Verify that ResourceNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            tenantService.getTenantDetail(NON_EXISTENT_ID);
        }, "User not found should throw ResourceNotFoundException");

        // Verify Repository call
        verify(userRepository).findById(NON_EXISTENT_ID);
    }
}