package com.rentora.api.service;

import com.rentora.api.model.dto.Contract.Request.CreateContractRequest;
import com.rentora.api.model.dto.Contract.Request.TerminateContractRequest;
import com.rentora.api.model.dto.Contract.Request.UpdateContractRequest;
import com.rentora.api.model.dto.Contract.Response.ContractDetailDto;
import com.rentora.api.model.dto.Contract.Response.ContractSummaryDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContractService {

    private final ContractRepository contractRepository;

    private final UnitRepository unitRepository;

    private final UserRepository userRepository;

    private final UnitUtilityRepository unitUtilityRepository;

    private final UtilityRepository utilityRepository;

    public Page<ContractSummaryDto> getContractsByApartment(UUID apartmentId, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByApartmentId(apartmentId, pageable);
        return contracts.map(this::toContractSummaryDto);
    }

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void expireContracts() {
        contractRepository.expireOldContracts();
    }

    public Page<ContractSummaryDto> getContractsByTenant(UUID tenantId, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByTenantId(tenantId, pageable);
        return contracts.map(this::toContractSummaryDto);
    }

    public ContractDetailDto getContractById(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return toContractDetailDto(contract);
    }

    public ContractDetailDto getContractByUnitId(UUID unitId) {

        Unit unit = unitRepository.findById(unitId).orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        Contract contract = unit.getContracts().stream().filter(a -> a.getStatus().equals(Contract.ContractStatus.active)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return toContractDetailDto(contract);
    }

    public ContractDetailDto createContract(CreateContractRequest request, UUID createdByUserId) {
        // Verify unit exists and is available
        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        if (unit.getStatus() != Unit.UnitStatus.available) {
            throw new BadRequestException("Unit is not available for rent");
        }

        // Check if unit already has an active contract
        if (contractRepository.findActiveContractByUnitId(request.getUnitId()).isPresent()) {
            throw new BadRequestException("Unit already has an active contract");
        }

        // Verify tenant exists
        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        User createdByUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Contract contract = new Contract();
        contract.setUnit(unit);
        contract.setTenant(tenant);
        contract.setGuarantorName(request.getGuarantorName());
        contract.setGuarantorPhone(request.getGuarantorPhone());
        contract.setGuarantorIdNumber(request.getGuarantorIdNumber());
        contract.setRentalType(request.getRentalType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setRentalPrice(request.getRentalPrice());
        contract.setDepositAmount(request.getDepositAmount());
        contract.setAdvancePaymentMonths(request.getAdvancePaymentMonths());
        contract.setLateFeeAmount(request.getLateFeeAmount());
        contract.setUtilitiesIncluded(request.getUtilitiesIncluded());
        contract.setTermsAndConditions(request.getTermsAndConditions());
        contract.setSpecialConditions(request.getSpecialConditions());
        contract.setStatus(Contract.ContractStatus.active);
        contract.setAutoRenewal(request.getAutoRenewal());
        contract.setRenewalNoticeDays(request.getRenewalNoticeDays());
        contract.setDocumentUrl(request.getDocumentUrl());
        contract.setCreatedByUser(createdByUser);

        //water start meter
        contract.setWaterMeterStartReading(request.getWaterMeterStart());

        //electric start meter
        contract.setElectricityMeterStartReading(request.getElectricMeterStart());



        Contract savedContract = contractRepository.save(contract);

        // Update unit status to occupied
        unit.setStatus(Unit.UnitStatus.occupied);
        unitRepository.save(unit);

        //get all utility
        List<Utility> utilities = utilityRepository.findByApartmentId(unit.getFloor().getBuilding().getApartment().getId());

        for(Utility utility : utilities) {
            UnitUtilities initialReading = new UnitUtilities();
            initialReading.setUnit(unit);
            initialReading.setReadingDate(contract.getStartDate());
            initialReading.setUsageMonth(contract.getStartDate().withDayOfMonth(1));
            initialReading.setUtility(utility);

            BigDecimal waterStart = request.getWaterMeterStart() != null ? request.getWaterMeterStart() : BigDecimal.ZERO;
            BigDecimal electricStart = request.getElectricMeterStart() != null ? request.getElectricMeterStart() : BigDecimal.ZERO;
            // Set meterStart from frontend DTO
            if (utility.getUtilityName().equalsIgnoreCase("electric")) {
                initialReading.setMeterStart(electricStart);
            } else if (utility.getUtilityName().equalsIgnoreCase("water")) {
                initialReading.setMeterStart(waterStart);
            } else {
                initialReading.setMeterStart(BigDecimal.ZERO); // default for others
            }

            initialReading.setMeterEnd(initialReading.getMeterStart());
            initialReading.setUsageAmount(BigDecimal.ZERO);

            unitUtilityRepository.save(initialReading);
        }


        log.info("Contract created: {} for unit: {} and tenant: {}",
                savedContract.getContractNumber(), unit.getUnitName(), tenant.getEmail());

        return toContractDetailDto(savedContract);
    }

    public ContractDetailDto updateContract(UUID contractId, UpdateContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (request.getGuarantorName() != null) contract.setGuarantorName(request.getGuarantorName());
        if (request.getGuarantorPhone() != null) contract.setGuarantorPhone(request.getGuarantorPhone());
        if (request.getGuarantorIdNumber() != null) contract.setGuarantorIdNumber(request.getGuarantorIdNumber());
        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());
        if (request.getRentalPrice() != null) contract.setRentalPrice(request.getRentalPrice());
        if (request.getDepositAmount() != null) contract.setDepositAmount(request.getDepositAmount());
        if (request.getAdvancePaymentMonths() != null) contract.setAdvancePaymentMonths(request.getAdvancePaymentMonths());
        if (request.getLateFeeAmount() != null) contract.setLateFeeAmount(request.getLateFeeAmount());
        if (request.getUtilitiesIncluded() != null) contract.setUtilitiesIncluded(request.getUtilitiesIncluded());
        if (request.getTermsAndConditions() != null) contract.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getSpecialConditions() != null) contract.setSpecialConditions(request.getSpecialConditions());
        if (request.getAutoRenewal() != null) contract.setAutoRenewal(request.getAutoRenewal());
        if (request.getRenewalNoticeDays() != null) contract.setRenewalNoticeDays(request.getRenewalNoticeDays());
        if (request.getDocumentUrl() != null) contract.setDocumentUrl(request.getDocumentUrl());
        if (request.getStatus() != null) contract.setStatus(request.getStatus());

        Contract savedContract = contractRepository.save(contract);

        log.info("Contract updated: {}", savedContract.getContractNumber());

        return toContractDetailDto(savedContract);
    }

    public ContractDetailDto terminateContract(UUID contractId, TerminateContractRequest request, UUID terminatedByUserId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (contract.getStatus() != Contract.ContractStatus.active) {
            throw new BadRequestException("Only active contracts can be terminated");
        }

        User terminatedByUser = userRepository.findById(terminatedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        contract.setStatus(Contract.ContractStatus.terminated);
        contract.setTerminationDate(request.getTerminationDate());
        contract.setTerminationReason(request.getTerminationReason());
        contract.setTerminatedByUser(terminatedByUser);

        Contract savedContract = contractRepository.save(contract);

        // Update unit status to available
        Unit unit = contract.getUnit();
        unit.setStatus(Unit.UnitStatus.available);
        unitRepository.save(unit);

        log.info("Contract terminated: {} for unit: {}", savedContract.getContractNumber(), unit.getUnitName());

        return toContractDetailDto(savedContract);
    }

    private ContractSummaryDto toContractSummaryDto(Contract contract) {
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

    private ContractDetailDto toContractDetailDto(Contract contract) {
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

        dto.setGuarantorName(contract.getGuarantorName());
        dto.setGuarantorPhone(contract.getGuarantorPhone());
        dto.setGuarantorIdNumber(contract.getGuarantorIdNumber());
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

        dto.setDocumentUrl(contract.getDocumentUrl());
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
            int durationDays = (int) ChronoUnit.DAYS.between(start, end) + 1; // +1 to include start day
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