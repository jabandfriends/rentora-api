package com.rentora.api.service;

import com.rentora.api.model.dto.Unit.Request.CreateUnitRequest;
import com.rentora.api.model.dto.Unit.Request.UpdateUnitRequest;
import com.rentora.api.model.dto.Unit.Response.UnitDetailDto;
import com.rentora.api.model.dto.Unit.Response.UnitSummaryDto;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.FloorRepository;
import com.rentora.api.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitService {

    private final UnitRepository unitRepository;

    private final FloorRepository floorRepository;

    private final ContractRepository contractRepository;

    public Page<UnitSummaryDto> getUnitsByApartment(UUID apartmentId, Unit.UnitStatus status,
                                                    Unit.UnitType unitType, UUID floorId, Pageable pageable) {
        Page<Unit> units;

        if (floorId != null) {
            units = unitRepository.findByFloorId(floorId, pageable);
        } else {
            units = unitRepository.findByApartmentIdAndFilters(apartmentId, status, unitType, pageable);
        }

        return units.map(this::toUnitSummaryDto);
    }

    public UnitDetailDto getUnitById(UUID unitId, UUID userId) {
        Unit unit = unitRepository.findByIdAndUserId(unitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found or access denied"));

        return toUnitDetailDto(unit);
    }

    public UnitDetailDto createUnit(CreateUnitRequest request, UUID userId) {
        // Verify floor exists and user has access
        Floor floor = floorRepository.findById(request.getFloorId())
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found"));

        // Check if unit name already exists on this floor
        if (unitRepository.existsByFloorIdAndUnitName(request.getFloorId(), request.getUnitName())) {
            throw new BadRequestException("Unit name already exists on this floor");
        }

        Unit unit = new Unit();
        unit.setFloor(floor);
        unit.setUnitName(request.getUnitName());
        unit.setUnitType(request.getUnitType());
        unit.setBedrooms(request.getBedrooms());
        unit.setBathrooms(request.getBathrooms());
        unit.setSquareMeters(request.getSquareMeters());
        unit.setBalconyCount(request.getBalconyCount());
        unit.setParkingSpaces(request.getParkingSpaces());
        unit.setStatus(Unit.UnitStatus.available);
        unit.setFurnishingStatus(request.getFurnishingStatus());
        unit.setFloorPlan(request.getFloorPlanUrl());
        unit.setNotes(request.getNotes());

        Unit savedUnit = unitRepository.save(unit);

        log.info("Unit created: {} in floor: {}", savedUnit.getUnitName(), floor.getFloorName());

        return toUnitDetailDto(savedUnit);
    }

    public UnitDetailDto updateUnit(UUID unitId, UpdateUnitRequest request, UUID userId) {
        Unit unit = unitRepository.findByIdAndUserId(unitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found or access denied"));

        if (request.getUnitName() != null) {
            // Check if new name conflicts with existing units on the same floor
            if (!unit.getUnitName().equals(request.getUnitName()) &&
                    unitRepository.existsByFloorIdAndUnitName(unit.getFloor().getId(), request.getUnitName())) {
                throw new BadRequestException("Unit name already exists on this floor");
            }
            unit.setUnitName(request.getUnitName());
        }
        if (request.getUnitType() != null) unit.setUnitType(request.getUnitType());
        if (request.getBedrooms() != null) unit.setBedrooms(request.getBedrooms());
        if (request.getBathrooms() != null) unit.setBathrooms(request.getBathrooms());
        if (request.getSquareMeters() != null) unit.setSquareMeters(request.getSquareMeters());
        if (request.getBalconyCount() != null) unit.setBalconyCount(request.getBalconyCount());
        if (request.getParkingSpaces() != null) unit.setParkingSpaces(request.getParkingSpaces());
        if (request.getStatus() != null) unit.setStatus(request.getStatus());
        if (request.getFurnishingStatus() != null) unit.setFurnishingStatus(request.getFurnishingStatus());
        if (request.getFloorPlanUrl() != null) unit.setFloorPlan(request.getFloorPlanUrl());
        if (request.getNotes() != null) unit.setNotes(request.getNotes());

        Unit savedUnit = unitRepository.save(unit);

        log.info("Unit updated: {}", savedUnit.getUnitName());

        return toUnitDetailDto(savedUnit);
    }

    public void deleteUnit(UUID unitId, UUID userId) {
        Unit unit = unitRepository.findByIdAndUserId(unitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found or access denied"));

        // Check if unit has active contracts
        if (contractRepository.findActiveContractByUnitId(unitId).isPresent()) {
            throw new BadRequestException("Cannot delete unit with active contract");
        }

        unitRepository.delete(unit);

        log.info("Unit deleted: {}", unit.getUnitName());
    }

    private UnitSummaryDto toUnitSummaryDto(Unit unit) {
        UnitSummaryDto dto = new UnitSummaryDto();
        dto.setId(unit.getId().toString());
        dto.setUnitName(unit.getUnitName());
        dto.setUnitType(unit.getUnitType());
        dto.setBedrooms(unit.getBedrooms());
        dto.setBathrooms(unit.getBathrooms());
        dto.setSquareMeters(unit.getSquareMeters());
        dto.setStatus(unit.getStatus());
        dto.setFurnishingStatus(unit.getFurnishingStatus());
        dto.setFloorName(unit.getFloor().getFloorName());
        dto.setBuildingName(unit.getFloor().getBuilding().getName());
        dto.setApartmentName(unit.getFloor().getBuilding().getApartment().getName());
        dto.setCreatedAt(unit.getCreatedAt() != null ? unit.getCreatedAt().toString() : null);

        // Get current tenant if any
        contractRepository.findActiveContractByUnitId(unit.getId())
                .ifPresent(contract -> {
                    if (contract.getTenant() != null) {
                        dto.setCurrentTenant(contract.getTenant().getFirstName() + " " + contract.getTenant().getLastName());
                    }
                });

        return dto;
    }

    private UnitDetailDto toUnitDetailDto(Unit unit) {
        UnitDetailDto dto = new UnitDetailDto();
        dto.setId(unit.getId().toString());
        dto.setUnitName(unit.getUnitName());
        dto.setUnitType(unit.getUnitType());
        dto.setBedrooms(unit.getBedrooms());
        dto.setBathrooms(unit.getBathrooms());
        dto.setSquareMeters(unit.getSquareMeters());
        dto.setBalconyCount(unit.getBalconyCount());
        dto.setParkingSpaces(unit.getParkingSpaces());
        dto.setStatus(unit.getStatus());
        dto.setFurnishingStatus(unit.getFurnishingStatus());
        dto.setFloorPlanUrl(unit.getFloorPlan());
        dto.setNotes(unit.getNotes());
        dto.setFloorId(unit.getFloor().getId().toString());
        dto.setFloorName(unit.getFloor().getFloorName());
        dto.setFloorNumber(unit.getFloor().getFloorNumber());
        dto.setBuildingId(unit.getFloor().getBuilding().getId().toString());
        dto.setBuildingName(unit.getFloor().getBuilding().getName());
        dto.setApartmentId(unit.getFloor().getBuilding().getApartment().getId().toString());
        dto.setApartmentName(unit.getFloor().getBuilding().getApartment().getName());
        dto.setCreatedAt(unit.getCreatedAt() != null ? unit.getCreatedAt().toString() : null);
        dto.setUpdatedAt(unit.getUpdatedAt() != null ? unit.getUpdatedAt().toString() : null);

        // Get current contract details
        contractRepository.findActiveContractByUnitId(unit.getId())
                .ifPresent(contract -> {
                    dto.setCurrentContractId(contract.getId().toString());
                    if (contract.getTenant() != null) {
                        dto.setCurrentTenantId(contract.getTenant().getId().toString());
                        dto.setCurrentTenantName(contract.getTenant().getFirstName() + " " + contract.getTenant().getLastName());
                        dto.setCurrentTenantEmail(contract.getTenant().getEmail());
                    }
                    dto.setCurrentRentalPrice(contract.getRentalPrice());
                });

        return dto;
    }
}