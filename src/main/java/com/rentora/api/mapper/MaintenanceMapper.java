package com.rentora.api.mapper;

import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceSupplyResponseDto;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.MaintenanceSupply;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.repository.MaintenanceSupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MaintenanceMapper {
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenanceSupplyRepository maintenanceSupplyRepository;

    public MaintenanceDetailDTO toMaintenanceDetailDto(Maintenance maintenance, LocalDate predictedSchedule, LocalDate predictedRecurringDate) {
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

        dto.setPredictedRecurringDate(predictedRecurringDate);
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

}
