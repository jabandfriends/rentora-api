package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Request.UpdateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.model.dto.Floor.Response.FloorResponseRequestDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.FloorRepository;
import com.rentora.api.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FloorService {
    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;



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

        long totalCurrentUnit = unitRepository.countByFloor(floor);
        if (totalCurrentUnit > 0) {
            throw new BadRequestException(
                    "This floor cannot be deleted because it has "
                            + totalCurrentUnit
                            + " unit(s) assigned to it. Please remove or reassign the units first."
            );
        }
        floorRepository.deleteById(floorId);
    }

    //update floor
    public void updateFloorById(UUID floorId,UpdateFloorRequestDto updateFloorRequestDto) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found"));
        Building building = buildingRepository.findById(updateFloorRequestDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        long totalCurrentUnit = unitRepository.countByFloor(floor);
        if (totalCurrentUnit > updateFloorRequestDto.getTotalUnits()) {
            throw new BadRequestException(
                    "Cannot reduce total units to "
                            + updateFloorRequestDto.getTotalUnits()
                            + " because there are already "
                            + totalCurrentUnit
                            + " unit(s) on this floor. Please increase the total units or remove some units first."
            );
        }
        Optional<Floor> existFloorNumber = floorRepository.findByBuildingAndFloorNumber(building,updateFloorRequestDto.getFloorNumber());
        if (existFloorNumber.isPresent() && !existFloorNumber.get().getId().equals(floor.getId())) {
            throw new BadRequestException(
                    "Cannot update floor. Floor number "
                            + updateFloorRequestDto.getFloorNumber()
                            + " is already in use in this building."
            );
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

        long totalCurrentTotalFloor = floorRepository.countByBuilding(building);
        if(building.getTotalFloors() == totalCurrentTotalFloor) {
            throw new BadRequestException(
                    "Cannot create a new floor. This building already has "
                            + totalCurrentTotalFloor
                            + " floor(s), which is the maximum allowed."
            );
        }

        Optional<Floor> existFloorNumber = floorRepository.findByBuildingAndFloorNumber(building,request.getFloorNumber());
        if(existFloorNumber.isPresent()) {
            throw new BadRequestException("Cannot create a new floor. This floor number is already in use. ");
        }
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
