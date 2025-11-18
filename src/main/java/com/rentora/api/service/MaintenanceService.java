package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.MaintenanceSupplyUsageRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceSupplyResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.*;
import com.rentora.api.specifications.MaintenanceSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final UnitRepository unitRepository;
    private final MaintenanceSupplyRepository  maintenanceSupplyRepository;
    private final SupplyTransactionService supplyTransactionService;

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


        BigDecimal totalPrice = BigDecimal.ZERO;
        List<MaintenanceSupply> maintenanceSupplyList = new ArrayList<>();


        // Save maintenance supply usage and calculate total price
        for (var supply : request.getSuppliesUsage()) {
            MaintenanceSupply maintenanceSupply = maintenanceSupplyService.maintenanceUseSupply(
                    maintenance,
                    supply.getSupplyId(),
                    supply.getSupplyUsedQuantity(),
                    userId
            );

            // Add cost to total price
            if(maintenanceSupply != null) {
                totalPrice = totalPrice.add(maintenanceSupply.getCost());
                maintenanceSupplyList.add(maintenanceSupply);
            }

        }

        maintenance.setActualCost(totalPrice);
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        for(MaintenanceSupply supply : maintenanceSupplyList) {
           MaintenanceSupply maintenanceSupply = maintenanceSupplyRepository.save(supply);
            supplyTransactionService.createMaintenanceUseSupplyTransaction(maintenanceSupply,userId);
        }

        return new ExecuteMaintenanceResponse(savedMaintenance.getId());

    }

    public ExecuteMaintenanceResponse updateMaintenance(UUID apartmentId,UUID userId,UUID maintenanceId, UpdateMaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));
        List<MaintenanceSupply> beforeMaintenanceData = maintenanceSupplyRepository.findByMaintenance(maintenance);


        List<MaintenanceSupplyResponseDto> currentMaintenance = request.getSuppliesUsage();

        // Track current IDs for deletion check
        Set<UUID> currentMaintenanceSupplyIds = currentMaintenance.stream()
                .map(MaintenanceSupplyResponseDto::getMaintenanceSupplyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Handle new + updated
        for (MaintenanceSupplyResponseDto current : currentMaintenance) {
            if (current.getMaintenanceSupplyId() == null) {
                //save maintenance supply usage
                maintenanceSupplyService.maintenanceUseSupply(maintenance,current.getSupplyId(),current.getSupplyUsedQuantity(),userId);
            } else {
                // Existing record → UPDATE
                maintenanceSupplyService.maintenanceUpdateSupply(apartmentId,current.getMaintenanceSupplyId(),
                        current.getSupplyId(),current.getSupplyUsedQuantity(),userId);
            }
        }

        List<MaintenanceSupply> toDeleteMaintenanceSupply = beforeMaintenanceData.stream()
                .filter(before -> !currentMaintenanceSupplyIds.contains(before.getId()))
                .toList();
        maintenanceSupplyService.removeMaintenanceList(apartmentId, toDeleteMaintenanceSupply, userId);
        maintenanceSupplyRepository.deleteAll(toDeleteMaintenanceSupply);

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + request.getUnitId()));

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

        OffsetDateTime nextMaintenanceDate = null;

        if (Boolean.TRUE.equals(maintenance.getIsRecurring())
                && maintenance.getRecurringSchedule() != null
                && maintenance.getAppointmentDate() != null) {

            OffsetDateTime lastMaintenance = maintenance.getAppointmentDate();
            Maintenance.RecurringSchedule schedule = maintenance.getRecurringSchedule();

            nextMaintenanceDate = switch (schedule) {
                case weekly    -> lastMaintenance.plusWeeks(1);
                case monthly   -> lastMaintenance.plusMonths(1);
                case quarterly -> lastMaintenance.plusMonths(3);
                case yearly    -> lastMaintenance.plusYears(1);
            };

            OffsetDateTime now = OffsetDateTime.now();
            while (!nextMaintenanceDate.isAfter(now)) {
                nextMaintenanceDate = switch (schedule) {
                    case weekly    -> nextMaintenanceDate.plusWeeks(1);
                    case monthly   -> nextMaintenanceDate.plusMonths(1);
                    case quarterly -> nextMaintenanceDate.plusMonths(3);
                    case yearly    -> nextMaintenanceDate.plusYears(1);
                };
            }
        }

        return toMaintenanceDetailDto(maintenance, nextMaintenanceDate);
    }


    public MaintenanceSupplyResponseDto toMaintenanceSupply(MaintenanceSupply maintenanceSupply) {
        return MaintenanceSupplyResponseDto.builder()
                .maintenanceSupplyId(maintenanceSupply.getId())
                .supplyUsedQuantity(maintenanceSupply.getQuantityUsed())

                //supply
                .supplyId(maintenanceSupply.getSupply().getId())
                .supplyName(maintenanceSupply.getSupply().getName())
                .supplyDescription(maintenanceSupply.getSupply().getDescription())
                .supplyCategory(maintenanceSupply.getSupply().getCategory())
                .supplyUnitPrice(maintenanceSupply.getSupply().getCostPerUnit())
                .supplyUnit(maintenanceSupply.getSupply().getUnit())
                .build();
    }
    public MaintenanceDetailDTO toMaintenanceDetailDto(Maintenance maintenance, OffsetDateTime predictedSchedule) {
        MaintenanceDetailDTO dto = new MaintenanceDetailDTO();

        if (maintenance == null) {
            return dto;
        }
        List<MaintenanceSupply> maintenanceSupplies = maintenanceSupplyRepository.findByMaintenance(maintenance);
        List<MaintenanceSupplyResponseDto> maintenanceSupply = maintenanceSupplies.stream().map(this::toMaintenanceSupply).toList();

        dto.setSuppliesUsage(maintenanceSupply);

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

        dto.setPredictedSchedule(predictedSchedule);

        return dto;
    }

    public MaintenanceInfoDTO toMaintenanceInfoDto(Maintenance maintenance) {

        String unitName = maintenance.getUnit().getUnitName();
        String buildingName = maintenance.getUnit().getFloor().getBuilding().getName();

        MaintenanceInfoDTO dto = new MaintenanceInfoDTO();

        dto.setId(maintenance.getId());
        dto.setTicketNumber(maintenance.getTicketNumber());
        dto.setTitle(maintenance.getTitle());
        dto.setActualCost(maintenance.getActualCost());

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
