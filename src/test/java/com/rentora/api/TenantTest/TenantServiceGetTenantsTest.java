package com.rentora.api.TenantTest;

import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.specifications.ApartmentUserSpecification;
import com.rentora.api.model.entity.ApartmentUser;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; // **FIXED: Import static for eq()**
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TenantServiceGetTenantsTest extends TenantServiceBaseTest {

    // ----------------------------------------------------------------------
    // 1. Happy Path: No Filters (status = null, name = null)
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldReturnAllTenants_WhenNoFiltersApplied() {
        // Arrange
        List<ApartmentUser> mockUsers = List.of(mockApartmentUserOccupied, mockApartmentUserUnoccupied);

        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            // Mock Spec chain
            Specification<ApartmentUser> mockSpec = mock(Specification.class);
            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(APARTMENT_ID)).thenReturn(mockSpec);
            mocked.when(() -> mockSpec.and(ApartmentUserSpecification.hasName(null))).thenReturn(mockSpec);

            // Mock Repository
            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(new PageImpl<>(mockUsers, PAGEABLE, mockUsers.size()));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants(null, null, APARTMENT_ID, PAGEABLE);

            // Assert
            assertEquals(2, result.getTotalElements());
            verify(apartmentUserRepository).findAll(any(Specification.class), eq(PAGEABLE));
        }
    }

    // ----------------------------------------------------------------------
    // 2. Happy Path: Filter by Active Status Only
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldFilterByActiveStatus_WhenStatusIsActive() {
        // Arrange
        String status = "active";
        List<ApartmentUser> mockActiveUsers = List.of(mockApartmentUserOccupied, mockApartmentUserUnoccupied);

        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            // Mock Spec chain: hasApartmentId -> and(hasName(null)) -> and(hasStatus(true))
            Specification<ApartmentUser> mockSpec1 = mock(Specification.class);
            Specification<ApartmentUser> mockSpec2 = mock(Specification.class);

            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(APARTMENT_ID)).thenReturn(mockSpec1);
            mocked.when(() -> mockSpec1.and(ApartmentUserSpecification.hasName(null))).thenReturn(mockSpec1);

            // Key: Mock hasStatus(true)
            mocked.when(() -> ApartmentUserSpecification.hasStatus(true)).thenReturn(mockSpec2);
            when(mockSpec1.and(mockSpec2)).thenReturn(mockSpec2);

            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(new PageImpl<>(mockActiveUsers, PAGEABLE, mockActiveUsers.size()));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants(status, null, APARTMENT_ID, PAGEABLE);

            // Assert
            assertEquals(2, result.getTotalElements());
            mocked.verify(() -> ApartmentUserSpecification.hasStatus(true));
        }
    }

    // ----------------------------------------------------------------------
    // 3. Happy Path: Filter by Inactive Status Only
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldFilterByInactiveStatus_WhenStatusIsNotActive() {
        // Arrange
        String status = "inactive";
        List<ApartmentUser> mockInactiveUsers = List.of(mockApartmentUserInactive);

        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            // Mock Spec chain: hasApartmentId -> and(hasName(null)) -> and(hasStatus(false))
            Specification<ApartmentUser> mockSpec1 = mock(Specification.class);
            Specification<ApartmentUser> mockSpec2 = mock(Specification.class);

            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(APARTMENT_ID)).thenReturn(mockSpec1);
            mocked.when(() -> mockSpec1.and(ApartmentUserSpecification.hasName(null))).thenReturn(mockSpec1);

            // Key: Mock hasStatus(false)
            mocked.when(() -> ApartmentUserSpecification.hasStatus(false)).thenReturn(mockSpec2);
            when(mockSpec1.and(mockSpec2)).thenReturn(mockSpec2);

            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(new PageImpl<>(mockInactiveUsers, PAGEABLE, mockInactiveUsers.size()));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants(status, null, APARTMENT_ID, PAGEABLE);

            // Assert
            assertEquals(1, result.getTotalElements());
            mocked.verify(() -> ApartmentUserSpecification.hasStatus(false));
        }
    }

    // ----------------------------------------------------------------------
    // 4. Happy Path: Filter by Name Only
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldFilterByName_WhenNameIsProvided() {
        // Arrange
        String nameFilter = "John Doe";
        List<ApartmentUser> mockNamedUsers = List.of(mockApartmentUserOccupied);

        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            // Mock Spec chain: hasApartmentId -> and(hasName(nameFilter))
            Specification<ApartmentUser> mockSpec1 = mock(Specification.class);
            Specification<ApartmentUser> mockSpec2 = mock(Specification.class);

            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(APARTMENT_ID)).thenReturn(mockSpec1);

            // Key: Mock hasName(nameFilter)
            mocked.when(() -> ApartmentUserSpecification.hasName(nameFilter)).thenReturn(mockSpec2);
            when(mockSpec1.and(mockSpec2)).thenReturn(mockSpec2);

            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(new PageImpl<>(mockNamedUsers, PAGEABLE, mockNamedUsers.size()));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants(null, nameFilter, APARTMENT_ID, PAGEABLE);

            // Assert
            assertEquals(1, result.getTotalElements());
            mocked.verify(() -> ApartmentUserSpecification.hasName(nameFilter));
        }
    }

    // ----------------------------------------------------------------------
    // 5. Happy Path: Filter by Both Name and Status
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldFilterByBothNameAndStatus_WhenBothAreProvided() {
        // Arrange
        String status = "active";
        String nameFilter = "John Doe";
        List<ApartmentUser> mockFilteredUsers = List.of(mockApartmentUserOccupied);

        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            // Mock Spec chain: hasApartmentId -> and(hasName(nameFilter)) -> and(hasStatus(true))
            Specification<ApartmentUser> mockSpec1 = mock(Specification.class);
            Specification<ApartmentUser> mockSpec2 = mock(Specification.class);
            Specification<ApartmentUser> mockSpec3 = mock(Specification.class);

            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(APARTMENT_ID)).thenReturn(mockSpec1);

            // 1. Name Filter
            mocked.when(() -> ApartmentUserSpecification.hasName(nameFilter)).thenReturn(mockSpec2);
            when(mockSpec1.and(mockSpec2)).thenReturn(mockSpec2);

            // 2. Status Filter
            mocked.when(() -> ApartmentUserSpecification.hasStatus(true)).thenReturn(mockSpec3);
            when(mockSpec2.and(mockSpec3)).thenReturn(mockSpec3);

            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(new PageImpl<>(mockFilteredUsers, PAGEABLE, mockFilteredUsers.size()));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants(status, nameFilter, APARTMENT_ID, PAGEABLE);

            // Assert
            assertEquals(1, result.getTotalElements());
            mocked.verify(() -> ApartmentUserSpecification.hasName(nameFilter));
            mocked.verify(() -> ApartmentUserSpecification.hasStatus(true));
        }
    }

    // ----------------------------------------------------------------------
    // 6. Edge Case: Empty Result
    // ----------------------------------------------------------------------
    @Test
    void getTenants_ShouldReturnEmptyPage_WhenNoMatchingTenantsFound() {
        // Arrange
        try (MockedStatic<ApartmentUserSpecification> mocked = mockStatic(ApartmentUserSpecification.class)) {
            Specification<ApartmentUser> mockSpec = mock(Specification.class);
            mocked.when(() -> ApartmentUserSpecification.hasApartmentId(any())).thenReturn(mockSpec);
            mocked.when(() -> mockSpec.and(any())).thenReturn(mockSpec);

            when(apartmentUserRepository.findAll(any(Specification.class), eq(PAGEABLE)))
                    .thenReturn(Page.empty(PAGEABLE));

            // Act
            Page<TenantInfoDto> result = tenantService.getTenants("active", "Nonexistent", APARTMENT_ID, PAGEABLE);

            // Assert
            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }
}