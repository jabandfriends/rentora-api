package com.rentora.api.service;

import com.rentora.api.model.dto.Building.Request.CreateBuildingRequest;
import com.rentora.api.model.dto.Building.Request.UpdateBuildingRequest;
import com.rentora.api.model.dto.Building.Response.BuildingDetailDto;
import com.rentora.api.model.dto.Building.Response.BuildingSummaryDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.repository.FloorRepository;
import com.rentora.api.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BuildingService {


    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final FloorRepository floorRepository;
    private final UnitRepository unitRepository;

    public Page<BuildingSummaryDto> getBuildingsByApartment(UUID apartmentId, String search, Pageable pageable) {
        Page<Building> buildings;

        if (search != null && !search.trim().isEmpty()) {
            buildings = buildingRepository.findByApartmentIdAndNameContaining(apartmentId, search.trim(), pageable);
        } else {
            buildings = buildingRepository.findByApartmentId(apartmentId, pageable);
        }

        return buildings.map(this::toBuildingSummaryDto);
    }

    public List<BuildingSummaryDto> getBuildingsByApartmentNoPaginate(UUID apartmentId) {
        List<Building> buildings = buildingRepository.findByApartmentId(apartmentId);
        return buildings.stream()
                .map(this::toBuildingSummaryDto)
                .collect(Collectors.toList());
    }

    public BuildingDetailDto getBuildingById(UUID buildingId, UUID userId) {
        Building building = buildingRepository.findByIdAndUserId(buildingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found or access denied"));

        return toBuildingDetailDto(building);
    }

    public BuildingDetailDto createBuilding(CreateBuildingRequest request, UUID userId) {
        // Verify apartment access
        Apartment apartment = apartmentRepository.findByIdAndUserId(request.getApartmentId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        // Check if building name already exists in apartment
        if (buildingRepository.existsByApartmentIdAndName(request.getApartmentId(), request.getName())) {
            throw new BadRequestException("Building name already exists in this apartment");
        }

        Building building = new Building();
        building.setApartment(apartment);
        building.setName(request.getName());
        building.setDescription(request.getDescription());
        building.setTotalFloors(request.getTotalFloors());
        building.setBuildingType(request.getBuildingType());
        building.setStatus(Building.BuildingStatus.active);

        Building savedBuilding = buildingRepository.save(building);

        log.info("Building created: {} in apartment: {}", savedBuilding.getName(), apartment.getName());

        return toBuildingDetailDto(savedBuilding);
    }

    public BuildingDetailDto updateBuilding(UUID buildingId, UpdateBuildingRequest request, UUID userId) {
        Building building = buildingRepository.findByIdAndUserId(buildingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found or access denied"));

        if (request.getName() != null) {
            // Check if new name conflicts with existing buildings
            if (!building.getName().equals(request.getName()) &&
                    buildingRepository.existsByApartmentIdAndName(building.getApartment().getId(), request.getName())) {
                throw new BadRequestException("Building name already exists in this apartment");
            }
            building.setName(request.getName());
        }
        if (request.getDescription() != null) building.setDescription(request.getDescription());
        if (request.getTotalFloors() != null) building.setTotalFloors(request.getTotalFloors());
        if (request.getBuildingType() != null) building.setBuildingType(request.getBuildingType());
        if (request.getStatus() != null) building.setStatus(request.getStatus());

        Building savedBuilding = buildingRepository.save(building);

        log.info("Building updated: {}", savedBuilding.getName());

        return toBuildingDetailDto(savedBuilding);
    }

    public void deleteBuilding(UUID buildingId, UUID userId) {
        Building building = buildingRepository.findByIdAndUserId(buildingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found or access denied"));

        // Check if building has units
        long unitCount = unitRepository.countByBuildingId(buildingId);
        if (unitCount > 0) {
            throw new BadRequestException("Cannot delete building with existing units");
        }

        buildingRepository.delete(building);

        log.info("Building deleted: {}", building.getName());
    }

    private BuildingSummaryDto toBuildingSummaryDto(Building building) {
        BuildingSummaryDto dto = new BuildingSummaryDto();
        dto.setId(building.getId().toString());
        dto.setName(building.getName());
        dto.setDescription(building.getDescription());
        dto.setTotalFloors(building.getTotalFloors());
        dto.setBuildingType(building.getBuildingType());
        dto.setStatus(building.getStatus());
        dto.setApartmentName(building.getApartment().getName());
        dto.setCreatedAt(building.getCreatedAt() != null ? building.getCreatedAt().toString() : null);

        // Get counts
        dto.setFloorCount(floorRepository.countByBuildingId(building.getId()));
        dto.setUnitCount(unitRepository.countByBuildingId(building.getId()));

        return dto;
    }

    private BuildingDetailDto toBuildingDetailDto(Building building) {
        BuildingDetailDto dto = new BuildingDetailDto();
        dto.setId(building.getId().toString());
        dto.setName(building.getName());
        dto.setDescription(building.getDescription());
        dto.setTotalFloors(building.getTotalFloors());
        dto.setBuildingType(building.getBuildingType());
        dto.setStatus(building.getStatus());
        dto.setApartmentId(building.getApartment().getId().toString());
        dto.setApartmentName(building.getApartment().getName());
        dto.setCreatedAt(building.getCreatedAt() != null ? building.getCreatedAt().toString() : null);
        dto.setUpdatedAt(building.getUpdatedAt() != null ? building.getUpdatedAt().toString() : null);

        // Get statistics
        dto.setFloorCount(floorRepository.countByBuildingId(building.getId()));
        dto.setUnitCount(unitRepository.countByBuildingId(building.getId()));
        dto.setAvailableUnits(unitRepository.countByBuildingIdAndStatus(building.getId(), Unit.UnitStatus.available));
        dto.setOccupiedUnits(unitRepository.countByBuildingIdAndStatus(building.getId(), Unit.UnitStatus.occupied));

        return dto;
    }
}