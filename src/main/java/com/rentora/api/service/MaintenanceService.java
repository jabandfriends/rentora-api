package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.ContractRepository;
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

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final UnitRepository unitRepository;

    private final MaintenanceSupplyService maintenanceSupplyService;

    public Page<MaintenanceInfoDTO> getMaintenance(UUID apartmentId, String name, Maintenance.Status status, Boolean isRecurring, UUID unitId,
                                                   Maintenance.Priority priority,Pageable pageable) {
        Specification<Maintenance> spec = MaintenanceSpecification.hasApartmentId(apartmentId).and(MaintenanceSpecification.hasName(name))
                .and(MaintenanceSpecification.hasRecurring(isRecurring)).and(MaintenanceSpecification.hasUnitId(unitId))
                .and(MaintenanceSpecification.hasPriority(priority));
        //status check
        if (status != null) {
            log.info("status: {}", status);
            spec = spec.and(MaintenanceSpecification.hasStatus(status));
        }
        Page<Maintenance> maintenance = maintenanceRepository.findAll(spec, pageable);

        return maintenance.map(this::toMaintenanceInfoDto);
    }

    public MaintenanceMetadataResponseDto getMaintenanceMetadata(UUID apartmentId) {

        long totalMaintenance =  maintenanceRepository.countMaintenanceByApartmentId(apartmentId);
        long totalCompleteMaintenances =  maintenanceRepository.countMaintenanceByStatusAndApartmentId(Maintenance.Status.completed, apartmentId);
        long totalPendingMaintenances = maintenanceRepository.countMaintenanceByStatusAndApartmentId(Maintenance.Status.pending, apartmentId);
        long totalInprogressMaintenances = maintenanceRepository.countMaintenanceByStatusAndApartmentId(Maintenance.Status.in_progress, apartmentId);
        long totalUrgentMaintenance = maintenanceRepository.countMaintenanceByApartmentAndPriority(apartmentId, Maintenance.Priority.urgent);

        return MaintenanceMetadataResponseDto.builder().totalMaintenance(totalMaintenance).completedCount(totalCompleteMaintenances)
                .pendingCount(totalPendingMaintenances).urgentCount(totalUrgentMaintenance).inProgressCount(totalInprogressMaintenances).build();

    }

    public ExecuteMaintenanceResponse createMaintenance(UUID userId, CreateMaintenanceRequest request) {

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + request.getUnitId()));

        //current ternant base on contract
        List<Contract> contracts = unit.getContracts();

        Optional<Contract> activeContract = contracts.stream()
                .filter(contract -> contract.getStatus().equals(Contract.ContractStatus.active))
                .findFirst();


        Maintenance maintenance = new Maintenance();

        //tenant from contract
        activeContract.ifPresent(contract -> maintenance.setTenantUser(contract.getTenant()));

        // from the DTO.
        maintenance.setUnit(unit);
        maintenance.setCategory(request.getCategory());
        maintenance.setTitle(request.getTitle());
        maintenance.setDescription(request.getDescription());
        maintenance.setPriority(request.getPriority());
        maintenance.setAppointmentDate(request.getAppointmentDate());
        maintenance.setDueDate(request.getDueDate());
        maintenance.setEstimatedHours(request.getEstimatedHours());
        maintenance.setEstimatedCost(request.getEstimatedCost());
        maintenance.setRequestedDate(LocalDate.now());
        maintenance.setIsEmergency(request.getIsEmergency());
        maintenance.setIsRecurring(request.getIsRecurring());
        if(request.getRecurringSchedule() != null ) {
            maintenance.setRecurringSchedule(request.getRecurringSchedule());
        }
        if (request.getStatus() != null) {
            maintenance.setStatus(request.getStatus());
        }



        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        //save maintenance supply usage
        request.getSuppliesUsage().forEach(supply -> {
            maintenanceSupplyService.maintenanceUseSupply(maintenance,supply.getSupplyId(),supply.getSupplyUsedQuantity(),userId);
        });

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
        if (request.getEstimatedCost() != null) maintenance.setEstimatedCost(request.getEstimatedCost());
        if(request.getIsEmergency() != null) maintenance.setIsEmergency(request.getIsEmergency());
        if(request.getIsRecurring() != null) maintenance.setIsRecurring(request.getIsRecurring());

        if(request.getIsRecurring() != null && !request.getIsRecurring()) {
            maintenance.setRecurringSchedule(null);
        }
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getStatus().equals(Maintenance.Status.completed)) {
            Maintenance.RecurringSchedule schedule = maintenance.getRecurringSchedule();

            if (schedule != null && maintenance.getAppointmentDate() != null) {
                // Assuming appointmentDate is LocalDateTime
                OffsetDateTime currentDateTime = maintenance.getAppointmentDate();
                OffsetDateTime offsetDateTime = currentDateTime; // start from current

                switch (schedule) {
                    case weekly:
                        offsetDateTime = currentDateTime.plusWeeks(1);
                        break;

                    case monthly:
                        offsetDateTime = currentDateTime.plusMonths(1);
                        break;

                    case quarterly:
                        offsetDateTime = currentDateTime.plusMonths(3);
                        break;

                    case yearly:
                        offsetDateTime = currentDateTime.plusYears(1);
                        break;

                    default:
                        maintenance.setRecurringSchedule(null);
                        break;
                }
                maintenance.setStatus(Maintenance.Status.pending);
                maintenance.setAppointmentDate(offsetDateTime);
            }
        }

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
            dto.setAppointmentDate(maintenance.getAppointmentDate());

        if (maintenance.getDueDate() != null)
            dto.setDueDate(maintenance.getDueDate());

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
            dto.setUnitId(maintenance.getUnit().getId());
        }

        // --- Tenant Info ---
        if (maintenance.getTenantUser() != null) {
            dto.setTenantName(maintenance.getTenantUser().getFullName());
            dto.setTenantEmail(maintenance.getTenantUser().getEmail());
            dto.setTenantPhoneNumber(maintenance.getTenantUser().getPhoneNumber());
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
            dto.setDueDate(maintenance.getDueDate());
        }
        if (maintenance.getAppointmentDate() != null) {
            dto.setAppointmentDate(maintenance.getAppointmentDate());
        }


        dto.setStatus(maintenance.getStatus());
        dto.setPriority(maintenance.getPriority());

        //recurring
        dto.setIsRecurring(maintenance.getIsRecurring());
        dto.setRecurringSchedule(maintenance.getRecurringSchedule());
        dto.setCreatedAt(maintenance.getCreatedAt());

        return dto;

    }
}
