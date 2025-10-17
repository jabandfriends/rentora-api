package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Request.UpdateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.model.dto.Floor.Response.FloorResponseRequestDto;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.FloorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FloorService {
    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;



    //get floors
    public List<FloorResponseRequestDto> getFloorByBuilding(UUID buildingId) {

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        List<Floor> floorList = floorRepository.findByBuilding(building);

        return floorList.stream().map(this::toFloorResponseRequestDto).toList();
    }

    //get floor detail by id
    public FloorResponseRequestDto getFloorById(UUID id) {
        Floor floor = floorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Floor not found"));
        return toFloorResponseRequestDto(floor);
    }

    //remove floor by id
    public void deleteFloorById(UUID floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found"));
        floorRepository.deleteById(floorId);
    }

    //update floor
    public void updateFloorById(UUID floorId,UpdateFloorRequestDto updateFloorRequestDto) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found"));

        if(updateFloorRequestDto.getBuildingId()!=null) {
            Building building = buildingRepository.findById(updateFloorRequestDto.getBuildingId())
                            .orElseThrow(()-> new ResourceNotFoundException("Building not found"));
            floor.setBuilding(building);
        }
        if(updateFloorRequestDto.getFloorName()!=null && !updateFloorRequestDto.getFloorName().isEmpty()) {
            floor.setFloorName(updateFloorRequestDto.getFloorName());
        }
        if(updateFloorRequestDto.getFloorNumber() != null){
            floor.setFloorNumber(updateFloorRequestDto.getFloorNumber());
        }
        if(updateFloorRequestDto.getTotalUnits() != null){
            floor.setTotalUnits(updateFloorRequestDto.getTotalUnits());
        }

        floorRepository.save(floor);
    }

    //create
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

    public FloorResponseRequestDto toFloorResponseRequestDto(Floor floor) {
        return FloorResponseRequestDto.builder()
                .floorId(floor.getId())
                .floorName(floor.getFloorName())
                .floorNumber(floor.getFloorNumber())
                .totalUnits(floor.getTotalUnits())
                .buildingId(floor.getBuilding().getId())
                .buildingName(floor.getBuilding().getName())
                .build();
    }
}
