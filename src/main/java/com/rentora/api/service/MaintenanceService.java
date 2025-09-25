package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenancePageResponse;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.User;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.ApartmentSpecification;
import com.rentora.api.specifications.MaintenanceSpecification;
import com.sun.tools.javac.Main;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.URL;
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

    public Page<MaintenanceDetailDTO> getMaintenance(UUID apartmentId, String name, Maintenance.Status status, Pageable pageable) {


        Specification<Maintenance> spec = MaintenanceSpecification.hasApartmentId(apartmentId).and(MaintenanceSpecification.hasName(name));
        //status check
        if (status != null) {
            log.info("status: {}", status);
            spec = spec.and(MaintenanceSpecification.hasStatus(status));
        }
        Page<Maintenance> maintenance = maintenanceRepository.findAll(spec, pageable);

        return maintenance.map(this::toMaintenanceDetailDto);
    }

    public MaintenanceMetadataResponseDto getMaintenanceMetadata(List<MaintenanceDetailDTO> maintenanceDetailDto) {
        MaintenanceMetadataResponseDto maintenanceMetadataResponseDto = new MaintenanceMetadataResponseDto();
        maintenanceMetadataResponseDto.setTotalMaintenance(maintenanceDetailDto.size());
        maintenanceMetadataResponseDto.setPendingCount(
                maintenanceDetailDto.stream()
                        .filter(dto -> dto.getStatus().equals(Maintenance.Status.pending.name()))
                        .count()
        );
        maintenanceMetadataResponseDto.setInProgressCount(
                maintenanceDetailDto.stream()
                        .filter(dto -> dto.getStatus().equals(Maintenance.Status.in_progress.name()))
                        .count()
        );
        maintenanceMetadataResponseDto.setAssignedCount(
                maintenanceDetailDto.stream()
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

        User tenantUser = userRepository.findById(request.getTenantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant user not found with ID: " + request.getTenantUserId()));

        // 2. Create a new Maintenance entity and map data.
        Maintenance maintenance = new Maintenance();

        //  from the DTO.
        maintenance.setTicketNumber(request.getTicketName()); // Implement your own logic for ticket number
        maintenance.setTitle(request.getTitle());
        maintenance.setDescription(request.getDescription());
        maintenance.setCategory(request.getCategory());
        maintenance.setPriority(request.getPriority());
        maintenance.setAppointmentDate(request.getAppointmentDate());
        maintenance.setEstimatedHours(request.getEstimatedHours());
        maintenance.setEstimatedCost(request.getEstimatedCost());
        maintenance.setIsEmergency(request.getIsEmergency());
        maintenance.setIsRecurring(request.getIsRecurring());
        maintenance.setRecurringSchedule(request.getRecurringSchedule());
        if (request.getStatus() != null) {
            maintenance.setStatus(request.getStatus());
        }

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        maintenance.setUnit(unit);
        maintenance.setTenantUser(tenantUser);

        return new ExecuteMaintenanceResponse(savedMaintenance.getId());

    }

    public ExecuteMaintenanceResponse updateMaintenance(UUID maintenanceId, UpdateMaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

        if (request.getTitle() != null) maintenance.setTitle(request.getTitle());
        if (request.getDescription() != null) maintenance.setDescription(request.getDescription());
        if (request.getStatus() != null) maintenance.setStatus(request.getStatus());
        if (request.getCategory() != null) maintenance.setCategory(request.getCategory());
        if (request.getPriority() != null) maintenance.setPriority(request.getPriority());
        if (request.getAppointmentDate() != null) maintenance.setAppointmentDate(request.getAppointmentDate());
        if (request.getStartAt() != null) maintenance.setStartedAt(request.getStartAt());
        if (request.getCompletedAt() != null) maintenance.setCompletedAt(request.getCompletedAt());
        if (request.getEstimatedHours() != null) maintenance.setEstimatedHours(request.getEstimatedHours());
        if (request.getActualHours() != null) maintenance.setActualHours(request.getActualHours());
        if (request.getEstimatedCost() != null) maintenance.setEstimatedCost(request.getEstimatedCost());
        if (request.getActualCost() != null) maintenance.setActualCost(request.getActualCost());
        if (request.getWorkSummary() != null) maintenance.setWorkSummary(request.getWorkSummary());
        if (request.getIsEmergency() != null) maintenance.setIsEmergency(request.getIsEmergency());
        if (request.getIsRecurring() != null) maintenance.setIsRecurring(request.getIsRecurring());

        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        log.info("Maintenance updated: {}", savedMaintenance.getTitle());

        return new ExecuteMaintenanceResponse(savedMaintenance.getId());
    }

        public void deleteMaintenance(UUID maintenanceId) {
            Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

            maintenanceRepository.delete(maintenance);

            log.info("maintenance deleted: {}", maintenance.getTitle());
        }

    public MaintenanceDetailDTO toMaintenanceDetailDto(Maintenance maintenance) {
        MaintenanceDetailDTO dto = new MaintenanceDetailDTO();
        dto.setId(maintenance.getId());
//        dto.setUnitId(maintenance.getUnit().getId());
//        dto.setTenantUserId(maintenance.getTenantUser().getId());
//        dto.setAssignedToUserId(maintenance.getAssignedToUser().getId());
        if (maintenance.getUnit() != null) {
            dto.setUnitId(maintenance.getUnit().getId());
        }
        if (maintenance.getTenantUser() != null) {
            dto.setTenantUserId(maintenance.getTenantUser().getId());
        }
        if (maintenance.getAssignedToUser() != null) {
            dto.setAssignedToUserId(maintenance.getAssignedToUser().getId());
        }
        dto.setTicketNumber(maintenance.getTicketNumber());
        dto.setTitle(maintenance.getTitle());
        dto.setDescription(maintenance.getDescription());
        dto.setCategory(maintenance.getCategory().name());
        dto.setStatus(maintenance.getStatus().name());
        dto.setPriority(maintenance.getPriority().name());
        dto.setRequestedDate(maintenance.getRequestedDate());
        if (maintenance.getAppointmentDate() != null) {
            dto.setAppointmentDate(maintenance.getAppointmentDate().toLocalDate());
        }
        if (maintenance.getStartedAt() != null) {
            dto.setStartedAt(maintenance.getStartedAt());
        }

        if (maintenance.getCompletedAt() != null) {
            dto.setCompletedAt(maintenance.getCompletedAt());
        }
        if (maintenance.getDueDate() != null) {
            dto.setDueDate(maintenance.getDueDate().toLocalDate());
        }
        dto.setEstimatedHours(maintenance.getEstimatedHours());
        dto.setActualHours(maintenance.getActualHours());
        dto.setEstimatedCost(maintenance.getEstimatedCost());
        dto.setActualCost(maintenance.getActualCost());
        dto.setWorkSummary(maintenance.getWorkSummary());
        dto.setTenantFeedback(maintenance.getTenantFeedback());
        dto.setTenantRating(maintenance.getTenantRating());
        dto.setIsEmergency(maintenance.getIsEmergency());
        dto.setIsRecurring(maintenance.getIsRecurring());
        if (maintenance.getCreatedAt() != null) {
            dto.setCreatedAt(maintenance.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (maintenance.getUpdatedAt() != null) {
            dto.setUpdatedAt(maintenance.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }

        return dto;
    }
}
