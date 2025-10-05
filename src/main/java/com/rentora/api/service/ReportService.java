package com.rentora.api.service;

import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.Pagination;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.repository.UtilityRepository;
import com.rentora.api.specifications.ReportSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final UnitUtilityRepository unitUtilityRepository;
    private final ContractRepository contractRepository;


    public Page<UnitServiceResponseDto> getUnitsUtility(UUID apartmentId,Pageable pageable) {
        Specification<UnitUtilities> reportUnitSpec = ReportSpecification.hasApartmentId(apartmentId);
        Page<UnitUtilities> units = unitUtilityRepository.findAll(reportUnitSpec,pageable);

        // group by unitId
        Map<UUID, List<UnitUtilities>> grouped = units.stream()
                .collect(Collectors.groupingBy(u -> u.getUnit().getId()));

        List<UnitServiceResponseDto> responses = grouped.values().stream()
                .map(this::toUnitServiceResponseDtoCombined)
                .toList();

        return new PageImpl<>(responses, pageable, units.getTotalElements());
    }

    private UnitServiceResponseDto toUnitServiceResponseDtoCombined(List<UnitUtilities> utilities) {
        UnitServiceResponseDto response = new UnitServiceResponseDto();

        // all utilities share same unit + tenant
        Unit unit = utilities.getFirst().getUnit();
        response.setRoomName(unit.getUnitName());

        Contract contract = contractRepository.findActiveContractByUnitId(unit.getId())
                .orElseThrow(() -> new EntityNotFoundException("Active contract not found"));

        response.setTenantName(contract.getTenant().getFullName());

        // fill water/electric fields depending on utility
        for (UnitUtilities u : utilities) {
            if (u.getUtility().getUtilityName().equalsIgnoreCase("water")) {
                response.setWaterUsage(u.getMeterEnd().subtract(u.getMeterStart()));
                response.setWaterCost(u.getCalculatedCost());
            }
            if (u.getUtility().getUtilityName().equalsIgnoreCase("electric")) {
                response.setElectricUsage(u.getMeterEnd().subtract(u.getMeterStart()));
                response.setElectricCost(u.getCalculatedCost());
            }
        }

        return response;
    }
    @Data
    public static class UnitServiceResponseDto {
        private String roomName;
        private String tenantName;
        private  BigDecimal electricUsage;
        private BigDecimal electricCost;
        private  BigDecimal waterUsage;
        private  BigDecimal waterCost;



    }


}
