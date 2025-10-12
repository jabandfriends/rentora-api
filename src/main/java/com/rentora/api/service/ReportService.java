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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final UnitUtilityRepository unitUtilityRepository;
    private final ContractRepository contractRepository;
    private final UnitRepository unitRepository;

    public Page<UnitServiceResponseDto> getUnitsUtility(UUID apartmentId,String unitName,String readingDate,Pageable pageable) {
        Specification<UnitUtilities> reportUnitSpec = ReportSpecification.hasApartmentId(apartmentId).and(ReportSpecification.hasName(unitName)).and(ReportSpecification.matchReadingDate(LocalDate.parse(readingDate)));
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
                .map(UnitUtilities::getReadingDate)     // only take the date
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
        private String roomName;
        private String tenantName;
        private  BigDecimal electricUsage;
        private BigDecimal electricCost;
        private  BigDecimal waterUsage;
        private  BigDecimal waterCost;



    }



}
