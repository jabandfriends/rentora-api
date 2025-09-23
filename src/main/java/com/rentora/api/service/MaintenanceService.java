package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.repository.MaintenanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceService {
    private MaintenanceRepository maintenanceRepository;

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
}
