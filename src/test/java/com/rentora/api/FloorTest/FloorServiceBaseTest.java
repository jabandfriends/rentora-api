package com.rentora.api.FloorTest;

import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Request.UpdateFloorRequestDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.FloorRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.service.FloorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public abstract class FloorServiceBaseTest {

    @Mock
    protected FloorRepository floorRepository;
    @Mock
    protected BuildingRepository buildingRepository;
    @Mock
    protected UnitRepository unitRepository;

    @InjectMocks
    protected FloorService floorService;

    protected final UUID APARTMENT_ID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    protected final UUID BUILDING_ID = UUID.fromString("20000000-0000-0000-0000-000000000000");
    protected final UUID FLOOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000000");
    protected final UUID ANOTHER_FLOOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    protected final UUID UNIT_ID = UUID.fromString("40000000-0000-0000-0000-000000000000");
    protected final UUID NON_EXISTENT_ID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

    protected Apartment mockApartment;
    protected Building mockBuilding;
    protected Floor mockFloor;
    protected Floor mockAnotherFloor;
    protected Unit mockUnit;

    protected CreateFloorRequestDto mockCreateRequest;
    protected UpdateFloorRequestDto mockUpdateRequest;


    @BeforeEach
    void setUpBase() {
        mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);

        mockBuilding = new Building();
        mockBuilding.setId(BUILDING_ID);
        mockBuilding.setName("Building A");
        mockBuilding.setApartment(mockApartment);
        mockBuilding.setTotalFloors(5);

        mockFloor = new Floor();
        mockFloor.setId(FLOOR_ID);
        mockFloor.setBuilding(mockBuilding);
        mockFloor.setFloorName("First Floor");
        mockFloor.setFloorNumber(1);
        mockFloor.setTotalUnits(10);

        mockAnotherFloor = new Floor();
        mockAnotherFloor.setId(ANOTHER_FLOOR_ID);
        mockAnotherFloor.setBuilding(mockBuilding);
        mockAnotherFloor.setFloorNumber(2);

        mockUnit = new Unit();
        mockUnit.setId(UNIT_ID);
        mockUnit.setFloor(mockFloor);

        mockCreateRequest = mock(CreateFloorRequestDto.class);
        mockCreateRequest.setBuildingId(BUILDING_ID);
        mockCreateRequest.setFloorName("Second Floor");
        mockCreateRequest.setFloorNumber(2);
        mockCreateRequest.setTotalUnits(12);

        mockUpdateRequest = mock(UpdateFloorRequestDto.class);
        mockUpdateRequest.setBuildingId(BUILDING_ID);
        mockUpdateRequest.setFloorName("First Floor - Updated");
        mockUpdateRequest.setFloorNumber(1);
        mockUpdateRequest.setTotalUnits(15);
    }
}
