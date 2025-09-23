package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.FloorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FloorService {
    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;

    public CreateFloorResponseDto createFloor(CreateFloorRequestDto request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        Floor floor = new Floor();
        floor.setBuilding(building);
        floor.setFloorName(request.getFloorName());
        floor.setFloorNumber(request.getFloorNumber());
        floor.setTotalUnits(request.getTotalUnits());

        Floor savedFloor = floorRepository.save(floor);

        return new CreateFloorResponseDto(savedFloor.getId());
    }
}
