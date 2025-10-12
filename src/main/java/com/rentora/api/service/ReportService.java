package com.rentora.api.service;

import com.rentora.api.model.dto.Report.Metadata.ReceiptReportMetaData;
import com.rentora.api.model.dto.Report.Metadata.ReportUnitUtilityMetadata;
import com.rentora.api.model.dto.Report.Metadata.RoomReportMetaData;
import com.rentora.api.model.dto.Report.Response.ReadingDateDto;
import com.rentora.api.model.dto.Report.Response.RoomReportDetailDTO;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.specifications.ReportSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final UnitUtilityRepository unitUtilityRepository;
    private final ContractRepository contractRepository;
    private final UnitRepository unitRepository;

    public Page<UnitServiceResponseDto> getUnitsUtility(UUID apartmentId,String unitName,String buildingName,String readingDate,Pageable pageable) {
        Specification<UnitUtilities> reportUnitSpec = ReportSpecification.hasApartmentId(apartmentId).and(ReportSpecification.hasName(unitName)).and(ReportSpecification.matchUsageDate(LocalDate.parse(readingDate)))
                .and(ReportSpecification.hasBuildingName(buildingName));
        Page<UnitUtilities> units = unitUtilityRepository.findAll(reportUnitSpec,pageable);

        // group by unitId
        Map<UUID, List<UnitUtilities>> grouped = units.stream()
                .collect(Collectors.groupingBy(u -> u.getUnit().getId()));

        List<UnitServiceResponseDto> responses = grouped.values().stream()
                .map(this::toUnitServiceResponseDtoCombined)
                .toList();

        return new PageImpl<>(responses, pageable, units.getTotalElements());
    }

    public List<ReadingDateDto> getUnitUtilityReadingDate(UUID apartmentId) {
        Specification<UnitUtilities> reportUnitSpec = ReportSpecification.hasApartmentId(apartmentId);
        List<UnitUtilities> units = unitUtilityRepository.findAll(reportUnitSpec);
        return units.stream()
                .map(UnitUtilities::getUsageMonth)     // only take the date
                .filter(Objects::nonNull)                // skip nulls
                .distinct()                              // remove duplicates
                .sorted()                                // optional: sort ascending
                .map(date -> ReadingDateDto.builder().readingDate(date).build())
                .toList();
    }

    public ReportUnitUtilityMetadata getUnitsUtilityMetadata(UUID apartmentId) {
        ReportUnitUtilityMetadata reportUnitUtilityMetadata = new ReportUnitUtilityMetadata();
        long waterUsageUnit = unitUtilityRepository.countUsageAmountByApartmentIdByUtility(apartmentId,"water");
        long electricUsageUnit = unitUtilityRepository.countUsageAmountByApartmentIdByUtility(apartmentId,"electric");

        BigDecimal waterUsagePrice = unitUtilityRepository.sumPriceByUtility(apartmentId,"water");
        BigDecimal electricUsagePrice = unitUtilityRepository.sumPriceByUtility(apartmentId,"electric");

        BigDecimal totalAmount = waterUsagePrice.add(electricUsagePrice);
        long totalUsageUnit = waterUsageUnit + electricUsageUnit;

        reportUnitUtilityMetadata.setWaterUsageUnits(waterUsageUnit);
        reportUnitUtilityMetadata.setElectricUsageUnits(electricUsageUnit);
        reportUnitUtilityMetadata.setWaterUsagePrices(waterUsagePrice);
        reportUnitUtilityMetadata.setElectricUsagePrices(electricUsagePrice);

        reportUnitUtilityMetadata.setTotalUsageUnits(totalUsageUnit);
        reportUnitUtilityMetadata.setTotalAmount(totalAmount);
        return reportUnitUtilityMetadata;
    }



    private UnitServiceResponseDto toUnitServiceResponseDtoCombined(List<UnitUtilities> utilities) {
        UnitServiceResponseDto response = new UnitServiceResponseDto();

        // all utilities share same unit
        Unit unit = utilities.getFirst().getUnit();
        response.setRoomName(unit.getUnitName());

        // Try to find active contract
        Optional<Contract> contractOpt = contractRepository.findActiveContractByUnitId(unit.getId());

        if (contractOpt.isPresent()) {
            Contract contract = contractOpt.get();
            response.setTenantName(contract.getTenant().getFullName());
            response.setBuildingName(contract.getUnit().getFloor().getBuilding().getName());
        } else {
            // Fallback if no active contract
            response.setTenantName("No tenant");
            response.setBuildingName(unit.getFloor().getBuilding().getName());
        }

        // Fill water/electric fields
        for (UnitUtilities u : utilities) {
            String utilityName = u.getUtility().getUtilityName().toLowerCase();

            if (utilityName.equals("water")) {
                response.setWaterUnitUtilityId(u.getId());
                response.setWaterUsage(u.getMeterEnd().subtract(u.getMeterStart()));
                response.setWaterMeterStart(u.getMeterStart());
                response.setWaterMeterEnd(u.getMeterEnd());
                response.setWaterCost(u.getCalculatedCost());
            }

            if (utilityName.equals("electric")) {
                response.setElectricUnitUtilityId(u.getId());
                response.setElectricUsage(u.getMeterEnd().subtract(u.getMeterStart()));
                response.setElectricMeterStart(u.getMeterStart());
                response.setElectricMeterEnd(u.getMeterEnd());
                response.setElectricCost(u.getCalculatedCost());
            }
        }

        return response;
    }
    public RoomReportMetaData getRoomReportMetadata(UUID apartmentId) {
        RoomReportMetaData metadata = new RoomReportMetaData();

        long totalRooms = unitRepository.countByFloor_Building_Apartment_Id(apartmentId);
        long availableRooms = unitRepository.countByFloor_Building_Apartment_IdAndStatus(
                apartmentId, Unit.UnitStatus.AVAILABLE
        );
        long unavailableRooms = unitRepository.countByFloor_Building_Apartment_IdAndStatus(
                apartmentId, Unit.UnitStatus.UNAVAILABLE
        );

        metadata.setTotalRooms(totalRooms);
        metadata.setAvailableRooms(availableRooms);
        metadata.setUnavailableRooms(unavailableRooms);

        return metadata;
    }
    public Page<RoomReportDetailDTO> getRoomReport(UUID apartmentId, Pageable pageable) {
        Specification<Unit> spec = (root, query, cb) ->
                apartmentId == null ? null : cb.equal(root.get("floor").get("building").get("apartment").get("id"), apartmentId);

        Page<Unit> units = unitRepository.findAll(spec, pageable);

        return units.map(unit -> {
            Contract contract = contractRepository.findActiveContractByUnitId(unit.getId())
                    .orElse(null);
            return toRoomReportDetailDTO(unit, contract);
        });
    }
    public RoomReportDetailDTO toRoomReportDetailDTO(Unit unit, Contract contract) {
        RoomReportDetailDTO dto = new RoomReportDetailDTO();
        dto.setRoomName(unit.getUnitName());
        dto.setTenantName(contract != null ? contract.getTenant().getFullName() : null);
        dto.setReservedName(contract != null ? contract.getGuarantorName() : null);
        dto.setTotalAmount(contract != null ? contract.getRentalPrice() : null);
        dto.setIssueDate(contract != null ? contract.getStartDate().toString() : null);
        dto.setDueDate(contract != null ? contract.getEndDate().toString() : null);
        dto.setCheckoutDate(contract != null ? contract.getEndDate().toString() : null);
        dto.setStatus(unit.getStatus().name());
        return dto;
    }

    @Data
    public static class UnitServiceResponseDto {
        private UUID waterUnitUtilityId;
        private UUID electricUnitUtilityId;
        private String roomName;
        private String buildingName;
        private String tenantName;
        private  BigDecimal electricUsage;
        private BigDecimal electricCost;
        private  BigDecimal waterUsage;
        private  BigDecimal waterCost;

        private  BigDecimal waterMeterStart;
        private  BigDecimal waterMeterEnd;


        private BigDecimal electricMeterStart;
        private  BigDecimal electricMeterEnd;







    }



}
