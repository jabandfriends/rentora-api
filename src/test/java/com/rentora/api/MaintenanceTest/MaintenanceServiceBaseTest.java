package com.rentora.api.MaintenanceTest;


import com.rentora.api.model.entity.*;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public abstract class MaintenanceServiceBaseTest {

    // 1. Dependencies (Repository Mocks)
    @Mock
    protected MaintenanceRepository maintenanceRepository;
    @Mock
    protected UnitRepository unitRepository;
    @Mock
    protected UserRepository userRepository;
    @Mock
    protected ContractRepository contractRepository;

    // 2. Class Under Test
    @InjectMocks
    protected MaintenanceService maintenanceService;

    // 3. UUIDs (Constants)
    protected final UUID APARTMENT_ID = UUID.fromString("11111111-aaaa-1111-aaaa-111111111111");
    protected final UUID BUILDING_ID = UUID.fromString("22222222-bbbb-2222-bbbb-222222222222");
    protected final UUID UNIT_ID = UUID.fromString("33333333-cccc-3333-cccc-333333333333");
    protected final UUID MAINTENANCE_ID = UUID.fromString("44444444-dddd-4444-dddd-444444444444");
    protected final UUID TENANT_USER_ID = UUID.fromString("55555555-eeee-5555-eeee-555555555555");

    // 4. Entity Objects
    protected Unit mockUnit;
    protected User mockTenantUser;
    protected Contract mockActiveContract;
    protected Maintenance mockPendingMaintenance;

    @BeforeEach
    void setUpBase() {
        // Mock Apartment (Required for Unit -> Floor -> Building -> Apartment check)
        Apartment mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);

        // Mock Building
        Building mockBuilding = new Building();
        mockBuilding.setId(BUILDING_ID);
        mockBuilding.setName("Building Alpha");
        mockBuilding.setApartment(mockApartment);

        // Mock Floor
        Floor mockFloor = new Floor();
        mockFloor.setBuilding(mockBuilding);

        // Mock Unit Entity
        mockUnit = new Unit();
        mockUnit.setId(UNIT_ID);
        mockUnit.setUnitName("A-101");
        mockUnit.setFloor(mockFloor);
        mockUnit.setContracts(Collections.emptyList());

        // Mock Tenant User
        mockTenantUser = new User();
        mockTenantUser.setId(TENANT_USER_ID);
        mockTenantUser.setFirstName("Tenant T. Test");
        mockTenantUser.setEmail("tenant@email.com");
        mockTenantUser.setPhoneNumber("0812345678");


        // Mock Active Contract
        mockActiveContract = new Contract();
        mockActiveContract.setUnit(mockUnit);
        mockActiveContract.setTenant(mockTenantUser);
        mockActiveContract.setStatus(Contract.ContractStatus.active);

        // Mock Maintenance Entity (Pending)
        mockPendingMaintenance = new Maintenance();
        mockPendingMaintenance.setId(MAINTENANCE_ID);
        mockPendingMaintenance.setTicketNumber("MAINT-001");
        mockPendingMaintenance.setTitle("Leaking Faucet");
        mockPendingMaintenance.setDescription("The kitchen faucet is dripping.");
        mockPendingMaintenance.setCategory(Maintenance.Category.plumbing);
        mockPendingMaintenance.setStatus(Maintenance.Status.pending);
        mockPendingMaintenance.setPriority(Maintenance.Priority.normal);
        mockPendingMaintenance.setRequestedDate(LocalDate.now());
        mockPendingMaintenance.setIsEmergency(false);
        mockPendingMaintenance.setIsRecurring(false);
        mockPendingMaintenance.setUnit(mockUnit);
    }
}
