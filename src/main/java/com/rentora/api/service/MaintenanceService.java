package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
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
import java.time.OffsetDateTime;
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;

    public Page<MaintenanceDetailDTO> getMaintenance(UUID apartmentId, String search, Maintenance.Status status, Pageable pageable) {


        Specification<Maintenance> spec = MaintenanceSpecification.hasApartmentId(apartmentId).and(MaintenanceSpecification.hasName(search)).and(MaintenanceSpecification.hasStatus(status));
        Page<Maintenance> maintenance = maintenanceRepository.findAll(pageable);

        return maintenance.map(maintenances -> {
            MaintenanceDetailDTO dto = toMaintenanceDetailDto(maintenances);

            return dto;
        });
    }

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

       //connect to other
//        maintenance.setUser(createdByUser);
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
    public MaintenanceDetailDTO getMaintenanceById(UUID maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found or access denied"));
        return toMaintenanceDetailDto(maintenance);
    }

    public void deleteMaintenance(UUID maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

        maintenanceRepository.delete(maintenance);

        log.info("maintenance deleted: {}", maintenance.getTitle());
    }

    private MaintenanceDetailDTO toMaintenanceDetailDto(Maintenance maintenance) {
        MaintenanceDetailDTO dto = new MaintenanceDetailDTO();
        dto.setId(maintenance.getId());
        dto.setTicketNumber(maintenance.getTicketNumber());
        dto.setUnitId(maintenance.getUnit().getId());
        dto.setTenantUserId(maintenance.getTenantUser().getId());
        dto.setAssignedToUserId(maintenance.getAssignedToUser().getId());
        dto.setTitle(maintenance.getTitle());
        dto.setDescription(maintenance.getDescription());
        dto.setCategory(maintenance.getCategory().name());
        dto.setStatus(maintenance.getStatus().name());
        dto.setPriority(maintenance.getPriority().name());
        dto.setRequestedDate(maintenance.getRequestedDate());
        dto.setAppointmentDate(maintenance.getAppointmentDate().toLocalDate());
        dto.setStartedAt(maintenance.getStartedAt());
        dto.setCompletedAt(maintenance.getCompletedAt());
        dto.setDueDate(maintenance.getDueDate().toLocalDate());
        dto.setEstimatedHours(maintenance.getEstimatedHours());
        dto.setActualHours(maintenance.getActualHours());
        dto.setEstimatedCost(maintenance.getEstimatedCost());
        dto.setRecurringSchedule(String.valueOf(maintenance.getRecurringSchedule()));
        dto.setCreatedAt(OffsetDateTime.from(maintenance.getCreatedAt()));
        dto.setUpdatedAt(OffsetDateTime.from(maintenance.getUpdatedAt()));
        if (maintenance.getAppointmentDate() != null) {
            dto.setAppointmentDate(maintenance.getAppointmentDate().toLocalDate());
        }
        if (maintenance.getStartedAt() != null) {
            dto.setStartedAt(maintenance.getStartedAt());
        }

        if (maintenance.getCompletedAt() != null) {
            dto.setCompletedAt(maintenance.getCompletedAt());
        }

        // Add a null check for getDueDate() as well
        if (maintenance.getDueDate() != null) {
            dto.setDueDate(maintenance.getDueDate().toLocalDate());
        }

        if (maintenance.getAssignedToUser() != null) {}

        dto.setActualCost(maintenance.getActualCost());
        dto.setWorkSummary(maintenance.getWorkSummary());
        dto.setTenantFeedback(maintenance.getTenantFeedback());
        dto.setTenantRating(maintenance.getTenantRating());
        dto.setIsEmergency(maintenance.getIsEmergency());
        dto.setIsRecurring(maintenance.getIsRecurring());
        return dto;
    }
}
