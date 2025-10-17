package com.rentora.api.TenantTest;

import com.rentora.api.model.dto.Authentication.FirstTimePasswordResetRequestDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.rentora.api.constant.enums.UserRole.tenant;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public abstract class TenantServiceBaseTest {

    @Mock
    protected ApartmentUserRepository apartmentUserRepository;
    @Mock
    protected UserRepository userRepository;
    @Mock
    protected ContractRepository contractRepository;
    @Mock
    protected PasswordEncoder passwordEncoder;

    @InjectMocks
    protected TenantService tenantService;

    // UUID Constants
    protected final UUID APARTMENT_ID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    protected final UUID USER_ID_OCCUPIED = UUID.fromString("20000000-0000-0000-0000-000000000002");
    protected final UUID USER_ID_INACTIVE = UUID.fromString("20000000-0000-0000-0000-000000000003");
    protected final UUID USER_ID_UNOCCUPIED = UUID.fromString("20000000-0000-0000-0000-000000000004");
    protected final UUID APARTMENT_USER_ID_OCCUPIED = UUID.fromString("30000000-0000-0000-0000-000000000002");
    protected final UUID APARTMENT_USER_ID_INACTIVE = UUID.fromString("30000000-0000-0000-0000-000000000003");
    protected final UUID NON_EXISTENT_ID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
    protected final LocalDateTime MOCK_CREATED_AT = LocalDateTime.of(2025, 1, 1, 10, 0);

    // Constants
    protected final Pageable PAGEABLE = PageRequest.of(0, 10);
    protected final String USER_NAME_ACTIVE = "John Doe";
    protected final String UNIT_NAME_1 = "ROOM A101";
    protected final String NEW_PASSWORD_HASH = "encodedPasswordHash123";

    // Mock Entities
    protected Apartment mockApartment;
    protected User mockUserOccupied;
    protected ApartmentUser mockApartmentUserOccupied;
    protected User mockUserUnoccupied;
    protected ApartmentUser mockApartmentUserUnoccupied;
    protected User mockUserInactive;
    protected ApartmentUser mockApartmentUserInactive;
    protected Contract mockActiveContract;

    protected FirstTimePasswordResetRequestDto mockPasswordResetDto;


    @BeforeEach
    void setUpBase() {
        // Core Entities
        // FIXED: mock Apartment object to allow lenient() stubbing
        mockApartment = mock(Apartment.class);
        lenient().when(mockApartment.getId()).thenReturn(APARTMENT_ID);
        lenient().when(mockApartment.getName()).thenReturn("Sample Apartment");

        // --- Active, Occupied Tenant (John Doe) ---
        // FIXED: Use SPY to allow real state changes (set/get)
        mockUserOccupied = spy(new User());
        mockUserOccupied.setId(USER_ID_OCCUPIED);
        mockUserOccupied.setFirstName(USER_NAME_ACTIVE);
        mockUserOccupied.setFirstName("John");
        mockUserOccupied.setLastName("Doe");
        mockUserOccupied.setEmail("john.doe@test.com");
        mockUserOccupied.setPhoneNumber("0812345678");
        mockUserOccupied.setCreatedAt(LocalDateTime.now());
        mockUserOccupied.setPasswordHash("oldHash"); // Initial password hash

        // Mock Contract for John Doe (Occupied)
        mockActiveContract = mock(Contract.class);
        lenient().when(mockActiveContract.getStatus()).thenReturn(Contract.ContractStatus.active);

        Unit mockUnit = mock(Unit.class);
        lenient().when(mockUnit.getUnitName()).thenReturn(UNIT_NAME_1);
        lenient().when(mockActiveContract.getUnit()).thenReturn(mockUnit);

        // Stubbing the getContracts method on the Spy
        lenient().when(mockUserOccupied.getContracts()).thenReturn(List.of(mockActiveContract));

        // ApartmentUser link for John Doe
        mockApartmentUserOccupied = new ApartmentUser();
        mockApartmentUserOccupied.setId(APARTMENT_USER_ID_OCCUPIED);
        mockApartmentUserOccupied.setUser(mockUserOccupied);
        mockApartmentUserOccupied.setIsActive(true);
        mockApartmentUserOccupied.setApartment(mockApartment);
        mockApartmentUserOccupied.setRole(tenant);
        mockApartmentUserOccupied.setCreatedAt(MOCK_CREATED_AT);


        // --- Active, Unoccupied Tenant (Jane Smith) ---
        // FIXED: Use SPY
        mockUserUnoccupied = spy(new User());
        mockUserUnoccupied.setId(USER_ID_UNOCCUPIED);
        mockUserUnoccupied.setFirstName("Jane Smith");
        mockUserUnoccupied.setEmail("jane.smith@test.com");
        lenient().when(mockUserUnoccupied.getContracts()).thenReturn(new ArrayList<>());

        mockApartmentUserUnoccupied = new ApartmentUser();
        mockApartmentUserUnoccupied.setUser(mockUserUnoccupied);
        mockApartmentUserUnoccupied.setIsActive(true);
        mockApartmentUserUnoccupied.setApartment(mockApartment);
        mockApartmentUserUnoccupied.setRole(tenant);
        mockApartmentUserUnoccupied.setCreatedAt(MOCK_CREATED_AT);


        // --- Inactive Tenant (User ID 3) ---
        // FIXED: Use SPY
        mockUserInactive = spy(new User());
        mockUserInactive.setId(USER_ID_INACTIVE);
        mockUserInactive.setFirstName("Inactive User");
        mockUserInactive.setEmail("inactive@test.com");
        lenient().when(mockUserInactive.getContracts()).thenReturn(new ArrayList<>());

        mockApartmentUserInactive = new ApartmentUser();
        mockApartmentUserInactive.setId(APARTMENT_USER_ID_INACTIVE);
        mockApartmentUserInactive.setUser(mockUserInactive);
        mockApartmentUserInactive.setIsActive(false);
        mockApartmentUserInactive.setApartment(mockApartment);
        mockApartmentUserInactive.setRole(tenant);
        mockApartmentUserInactive.setCreatedAt(MOCK_CREATED_AT);

        // --- DTO Mocks ---
        mockPasswordResetDto = new FirstTimePasswordResetRequestDto();
        mockPasswordResetDto.setNewPassword("newSecurePassword123");
    }
}