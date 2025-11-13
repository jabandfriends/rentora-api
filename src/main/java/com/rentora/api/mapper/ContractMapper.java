package com.rentora.api.mapper;

import com.rentora.api.model.dto.Contract.Response.ContractDetailDto;
import com.rentora.api.model.dto.Contract.Response.ContractSummaryDto;
import com.rentora.api.model.dto.Contract.Response.ContractUpdateResponseDto;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class ContractMapper {
    private final S3FileService s3FileService;
    public ContractUpdateResponseDto toContractUpdateResponseDto(URL presignedURL, Contract contract) {
        return ContractUpdateResponseDto.builder()
                .presignedUrl(presignedURL)
                .contractId(contract.getId())
                .build();
    }
    public ContractSummaryDto toContractSummaryDto(Contract contract) {
        ContractSummaryDto dto = new ContractSummaryDto();
        dto.setId(contract.getId().toString());
        dto.setContractNumber(contract.getContractNumber());
        dto.setUnitName(contract.getUnit().getUnitName());
        dto.setBuildingName(contract.getUnit().getFloor().getBuilding().getName());
        dto.setApartmentName(contract.getUnit().getFloor().getBuilding().getApartment().getName());

        if (contract.getTenant() != null) {
            dto.setTenantName(contract.getTenant().getFirstName() + " " + contract.getTenant().getLastName());
            dto.setTenantEmail(contract.getTenant().getEmail());
        }

        dto.setRentalType(contract.getRentalType());
        dto.setStartDate(contract.getStartDate() != null ? contract.getStartDate().toString() : null);
        dto.setEndDate(contract.getEndDate() != null ? contract.getEndDate().toString() : null);
        dto.setRentalPrice(contract.getRentalPrice());
        dto.setStatus(contract.getStatus());
        dto.setCreatedAt(contract.getCreatedAt() != null ? contract.getCreatedAt().toString() : null);

        return dto;
    }
    public ContractDetailDto toContractDetailDto(Contract contract) {
        ContractDetailDto dto = new ContractDetailDto();

        dto.setContractId(contract.getId());
        dto.setContractNumber(contract.getContractNumber());
        dto.setUnitName(contract.getUnit().getUnitName());
        dto.setBuildingName(contract.getUnit().getFloor().getBuilding().getName());
        dto.setApartmentName(contract.getUnit().getFloor().getBuilding().getApartment().getName());

        if (contract.getTenant() != null) {
            dto.setTenantName(contract.getTenant().getFirstName() + " " + contract.getTenant().getLastName());
            dto.setTenantEmail(contract.getTenant().getEmail());
            dto.setTenantPhone(contract.getTenant().getPhoneNumber());
        }

        dto.setRentalType(contract.getRentalType());
        dto.setStartDate(contract.getStartDate() != null ? contract.getStartDate().toString() : null);
        dto.setEndDate(contract.getEndDate() != null ? contract.getEndDate().toString() : null);
        dto.setRentalPrice(contract.getRentalPrice());
        dto.setDepositAmount(contract.getDepositAmount());
        dto.setAdvancePaymentMonths(contract.getAdvancePaymentMonths());
        dto.setLateFeeAmount(contract.getLateFeeAmount());
        dto.setUtilitiesIncluded(contract.getUtilitiesIncluded());
        dto.setTermsAndConditions(contract.getTermsAndConditions());
        dto.setSpecialConditions(contract.getSpecialConditions());
        dto.setStatus(contract.getStatus());
        dto.setAutoRenewal(contract.getAutoRenewal());
        dto.setRenewalNoticeDays(contract.getRenewalNoticeDays());
        dto.setTerminationDate(contract.getTerminationDate() != null ? contract.getTerminationDate().toString() : null);
        dto.setTerminationReason(contract.getTerminationReason());

        if (contract.getTerminatedByUser() != null) {
            dto.setTerminatedByUserName(contract.getTerminatedByUser().getFirstName() + " " + contract.getTerminatedByUser().getLastName());
        }

        if(contract.getDocumentUrl() != null && !contract.getDocumentUrl().isEmpty()) {
            URL fileURL = s3FileService.generatePresignedUrlForGet(contract.getDocumentUrl());
            dto.setDocumentUrl(fileURL.toString());
        }

        dto.setSignedAt(contract.getSignedAt() != null ? contract.getSignedAt().toString() : null);

        //utility
        dto.setWaterMeterStart(contract.getWaterMeterStartReading());
        dto.setElectricMeterStart(contract.getElectricityMeterStartReading());
        if (contract.getCreatedByUser() != null) {
            dto.setCreatedByUserName(contract.getCreatedByUser().getFirstName() + " " + contract.getCreatedByUser().getLastName());
        }

        dto.setCreatedAt(contract.getCreatedAt() != null ? contract.getCreatedAt().toString() : null);
        dto.setUpdatedAt(contract.getUpdatedAt() != null ? contract.getUpdatedAt().toString() : null);

        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            LocalDate start = contract.getStartDate();
            LocalDate end = contract.getEndDate();

            // Contract duration in days
            int durationDays = (int) ChronoUnit.DAYS.between(start, end);// +1 to include start day
            dto.setContractDurationDays(durationDays);

            // Days until expiry
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), end);
            dto.setDaysUntilExpiry(daysUntilExpiry >= 0 ? daysUntilExpiry : 0); // 0 if already expired
        } else {
            dto.setContractDurationDays(0);
            dto.setDaysUntilExpiry(0L);
        }

        return dto;
    }
}
