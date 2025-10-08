package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.MaintenanceSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;

    public Page<MaintenanceInfoDTO> getMaintenance(UUID apartmentId, String name, Maintenance.Status status, Pageable pageable) {


        Specification<Maintenance> spec = MaintenanceSpecification.hasApartmentId(apartmentId).and(MaintenanceSpecification.hasName(name));
        //status check
        if (status != null) {
            log.info("status: {}", status);
            spec = spec.and(MaintenanceSpecification.hasStatus(status));
        }
        Page<Maintenance> maintenance = maintenanceRepository.findAll(spec, pageable);

        return maintenance.map(this::toMaintenanceInfoDto);
    }

    public MaintenanceMetadataResponseDto getMaintenanceMetadata(List<MaintenanceInfoDTO> maintenanceInfoDto) {
        MaintenanceMetadataResponseDto maintenanceMetadataResponseDto = new MaintenanceMetadataResponseDto();
        maintenanceMetadataResponseDto.setTotalMaintenance(maintenanceInfoDto.size());
        maintenanceMetadataResponseDto.setPendingCount(
                maintenanceInfoDto.stream()
                        .filter(dto -> dto.getStatus().equals(Maintenance.Status.pending.name()))
                        .count()
        );
        maintenanceMetadataResponseDto.setInProgressCount(
                maintenanceInfoDto.stream()
                        .filter(dto -> dto.getStatus().equals(Maintenance.Status.in_progress.name()))
                        .count()
        );
        maintenanceMetadataResponseDto.setAssignedCount(
                maintenanceInfoDto.stream()
                        .filter(dto -> dto.getStatus().equals(Maintenance.Status.assigned.name()))
                        .count()
        );

        return maintenanceMetadataResponseDto;

    }


//        List<MaintenanceDetailDTO> maintenanceDTOS = maintenance.map(MaintenanceService::toMaintenanceDetailDto).getContent();
//        long totalMaintenance = maintenance.getTotalElements();
//        long pending = maintenance.stream()
//                .filter(m -> m.getStatus() == Maintenance.Status.pending)
//                .count();
//        long in_progress = maintenance.stream().filter(m -> m.getStatus() == Maintenance.Status.in_progress).count();
//        long assigned = maintenance.stream().filter(m -> m.getStatus() == Maintenance.Status.assigned).count();
//
////        return maintenance.map(maintenances -> {
////            MaintenanceDetailDTO dto = toMaintenanceDetailDto(maintenances);
//        // Create the response object and populate it
//        MaintenancePageResponse response = new MaintenancePageResponse();
//        response.setMaintenances(maintenanceDTOS);
//        response.setTotalMaintenance(totalMaintenance);
//        response.setPendingCount(pending);
//        response.setAssignedCount(assigned);
//        response.setInProgressCount(in_progress);
//        response.setCurrentPage(maintenance.getNumber());
//        response.setTotalPages(maintenance.getTotalPages());
//
//        return response;
//    };


    public ExecuteMaintenanceResponse createMaintenance(UUID createByUserId, CreateMaintenanceRequest request) {
        // 1. Fetch related entities from the database.
    //        User createdByUser = userRepository.findById(createByUserId)
    //                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with ID: " + createByUserId));

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + request.getUnitId()));
        String unitName = unit.getUnitName();

//        User tenantUser = userRepository.findById(request.getTenantUserId())
//                .orElseThrow(() -> new ResourceNotFoundException("Tenant user not found with ID: " + request.getTenantUserId()));


        // 2. Create a new Maintenance entity and map data.
        Maintenance maintenance = new Maintenance();


        //  from the DTO.
        maintenance.setUnit(unit);

        maintenance.setTicketNumber(request.getTicketNumber());
        maintenance.setTitle(request.getTitle());
        maintenance.setDescription(request.getDescription());
        maintenance.setPriority(request.getPriority());
        maintenance.setAppointmentDate(request.getAppointmentDate());
        maintenance.setDueDate(request.getDueDate());
        maintenance.setEstimatedHours(request.getEstimatedHours());
        if (request.getStatus() != null) {
            maintenance.setStatus(request.getStatus());
        }


        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        maintenance.setUnit(unit);
//        maintenance.setTenantUser(tenantUser);


        return new ExecuteMaintenanceResponse(savedMaintenance.getId());

    }

    public ExecuteMaintenanceResponse updateMaintenance(UUID maintenanceId, UpdateMaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + request.getUnitId()));
        String unitName = unit.getUnitName();

        if (request.getTitle() != null) maintenance.setTitle(request.getTitle());
        if (request.getDescription() != null) maintenance.setDescription(request.getDescription());
        if (request.getStatus() != null) maintenance.setStatus(request.getStatus());
        if (request.getCategory() != null) maintenance.setCategory(request.getCategory());
        if (request.getPriority() != null) maintenance.setPriority(request.getPriority());
        if (request.getAppointmentDate() != null) maintenance.setAppointmentDate(request.getAppointmentDate());
        if (request.getDueDate() != null) maintenance.setDueDate(request.getDueDate());
        if (request.getEstimatedHours() != null) maintenance.setEstimatedHours(request.getEstimatedHours());
        if (request.getRecurringSchedule() != null) maintenance.setRecurringSchedule(request.getRecurringSchedule());
        if (request.getUnitId() != null) maintenance.setUnit(unit);

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        log.info("Maintenance updated: {}", savedMaintenance.getTitle());

        return new ExecuteMaintenanceResponse(savedMaintenance.getId());
    }

        public void deleteMaintenance(UUID maintenanceId) {
            Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

            maintenanceRepository.delete(maintenance);

            log.info("maintenance deleted: {}", maintenance.getTitle());
        }

    public MaintenanceDetailDTO getMaintenanceById(UUID maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found or access denied"));
        return toMaintenanceDetailDto(maintenance);
    }

    public MaintenanceDetailDTO toMaintenanceDetailDto(Maintenance maintenance) {
        MaintenanceDetailDTO dto = new MaintenanceDetailDTO();

        if (maintenance == null) {
            return dto;
        }

        // --- Basic Maintenance Info ---
        dto.setId(maintenance.getId());
        dto.setUnitId(maintenance.getUnit().getId());
        dto.setTicketNumber(maintenance.getTicketNumber());
        dto.setTitle(maintenance.getTitle());
        dto.setDescription(maintenance.getDescription());

        if (maintenance.getCategory() != null)
            dto.setCategory(maintenance.getCategory());

        if (maintenance.getStatus() != null)
            dto.setStatus(maintenance.getStatus());

        if (maintenance.getPriority() != null)
            dto.setPriority(maintenance.getPriority());

        dto.setRequestedDate(maintenance.getRequestedDate());

        if (maintenance.getAppointmentDate() != null)
            dto.setAppointmentDate(maintenance.getAppointmentDate().toLocalDate());

        if (maintenance.getDueDate() != null)
            dto.setDueDate(maintenance.getDueDate().toLocalDate());

        dto.setStartedAt(maintenance.getStartedAt());
        dto.setCompletedAt(maintenance.getCompletedAt());
        dto.setEstimatedHours(maintenance.getEstimatedHours());
        dto.setActualHours(maintenance.getActualHours());
        dto.setEstimatedCost(maintenance.getEstimatedCost());
        dto.setActualCost(maintenance.getActualCost());
        dto.setWorkSummary(maintenance.getWorkSummary());
        dto.setTenantFeedback(maintenance.getTenantFeedback());
        dto.setTenantRating(maintenance.getTenantRating());
        dto.setIsEmergency(maintenance.getIsEmergency());
        dto.setIsRecurring(maintenance.getIsRecurring());

        if (maintenance.getRecurringSchedule() != null)
            dto.setRecurringSchedule(maintenance.getRecurringSchedule());

        if (maintenance.getCreatedAt() != null)
            dto.setCreatedAt(maintenance.getCreatedAt());

        if (maintenance.getUpdatedAt() != null)
            dto.setUpdatedAt(maintenance.getUpdatedAt());

        // --- Building Info ---
        if (maintenance.getUnit() != null) {
            if (maintenance.getUnit().getFloor() != null &&
                    maintenance.getUnit().getFloor().getBuilding() != null) {
                dto.setBuildingsName(maintenance.getUnit().getFloor().getBuilding().getName());
            }
            dto.setUnitName(maintenance.getUnit().getUnitName());
        }


        return dto;
    }


    public MaintenanceInfoDTO toMaintenanceInfoDto(Maintenance maintenance) {

        String unitName = maintenance.getUnit().getUnitName();
        String buildingName = maintenance.getUnit().getFloor().getBuilding().getName();

        MaintenanceInfoDTO dto = new MaintenanceInfoDTO();

        dto.setId(maintenance.getId());
        dto.setTicketNumber(maintenance.getTicketNumber());
        dto.setTitle(maintenance.getTitle());

        if (unitName != null) {
            dto.setUnitName(unitName);
        }
        if (buildingName != null) {
            dto.setBuildingsName(buildingName);
        }
        if (maintenance.getDueDate() != null) {
            dto.setDueDate(maintenance.getDueDate().toLocalDate());
        }
        if (maintenance.getAppointmentDate() != null) {
            dto.setAppointmentDate(maintenance.getAppointmentDate().toLocalDate());
        }


        dto.setStatus(maintenance.getStatus().name());





        return dto;

    }
}
