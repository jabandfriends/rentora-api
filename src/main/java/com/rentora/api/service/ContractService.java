package com.rentora.api.service;

import com.rentora.api.mapper.ContractMapper;
import com.rentora.api.model.dto.Contract.Request.CreateContractRequest;
import com.rentora.api.model.dto.Contract.Request.TerminateContractRequest;
import com.rentora.api.model.dto.Contract.Request.UpdateContractRequest;
import com.rentora.api.model.dto.Contract.Response.ContractDetailDto;
import com.rentora.api.model.dto.Contract.Response.ContractSummaryDto;
import com.rentora.api.model.dto.Contract.Response.ContractUpdateResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.*;
import com.rentora.api.specifications.ContractSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;

    private final S3FileService s3FileService;

    private final ContractMapper contractMapper;



    public Page<ContractSummaryDto> getContractsByApartment(UUID apartmentId, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByApartmentId(apartmentId, pageable);
        return contracts.map(contractMapper::toContractSummaryDto);
    }

    public Page<ContractSummaryDto> getContractsByStatusByApartmentIdByUnit(UUID apartmentId, Contract.ContractStatus contractStatus,
                                                                      UUID unitId,Pageable pageable) {
        Specification<Contract> contractSpecification = ContractSpecification.hasStatus(contractStatus)
                        .and(ContractSpecification.hasApartmentId(apartmentId)).and(ContractSpecification.hasUnitId(unitId));
        Page<Contract> contracts = contractRepository.findAll(contractSpecification,pageable);

        return contracts.map(contractMapper::toContractSummaryDto);

    }

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void expireContracts() {
        contractRepository.expireOldContracts();
    }

    public Page<ContractSummaryDto> getContractsByTenant(UUID tenantId, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByTenantId(tenantId, pageable);
        return contracts.map(contractMapper::toContractSummaryDto);
    }

    public ContractDetailDto getContractById(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return contractMapper.toContractDetailDto(contract);
    }

    public ContractDetailDto getContractByUnitId(UUID unitId) {

        Unit unit = unitRepository.findById(unitId).orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        Contract contract = unit.getContracts().stream().filter(a -> a.getStatus().equals(Contract.ContractStatus.active)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return contractMapper.toContractDetailDto(contract);
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
        contract.setRentalType(request.getRentalType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setRentalPrice(request.getRentalPrice());
        contract.setDepositAmount(request.getDepositAmount());
        contract.setAdvancePaymentMonths(request.getAdvancePaymentMonths());
        contract.setLateFeeAmount(unit.getFloor().getBuilding().getApartment().getLateFee());

        if(request.getRentalType().equals(Contract.RentalType.daily)){
            contract.setUtilitiesIncluded(false);
        }else{
            contract.setUtilitiesIncluded(true);
        }

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


        log.info("Contract created: {} for unit: {} and tenant: {}",
                savedContract.getContractNumber(), unit.getUnitName(), tenant.getEmail());

        return contractMapper.toContractDetailDto(savedContract);
    }

    public ContractUpdateResponseDto updateContract(UUID apartmentId,UUID contractId, UpdateContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));

        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());
        if (request.getRentalPrice() != null) contract.setRentalPrice(request.getRentalPrice());
        if (request.getDepositAmount() != null) contract.setDepositAmount(request.getDepositAmount());
        if (request.getAdvancePaymentMonths() != null) contract.setAdvancePaymentMonths(request.getAdvancePaymentMonths());
        if (request.getTermsAndConditions() != null) contract.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getSpecialConditions() != null) contract.setSpecialConditions(request.getSpecialConditions());
        if (request.getAutoRenewal() != null) contract.setAutoRenewal(request.getAutoRenewal());
        if (request.getRenewalNoticeDays() != null) contract.setRenewalNoticeDays(request.getRenewalNoticeDays());
        if (request.getStatus() != null) contract.setStatus(request.getStatus());


        String fileKey = null;
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));


        URL presignedURL = null;

        if (request.getDocumentFilename() != null && !request.getDocumentFilename().isEmpty()) {
            if(contract.getDocumentUrl() != null && !contract.getDocumentUrl().isEmpty()){
                s3FileService.deleteFile(contract.getDocumentUrl());
            }
            //get file name
            fileKey = "apartments/contract/"+apartment.getId()+"/"+ timestamp+"contract-signed"+ contract.getTenant().getFirstName()+request.getDocumentFilename();
            try{
                presignedURL = s3FileService.generatePresignedUrlForPut(fileKey);
                contract.setDocumentUrl(fileKey);
                contract.setSignedAt(LocalDateTime.now());
            }catch(Exception e){
                log.info("Failed to put signed contract to database");
            }
        }


        Contract savedContract = contractRepository.save(contract);

        log.info("Contract updated: {}", savedContract.getContractNumber());

        return contractMapper.toContractUpdateResponseDto(presignedURL,savedContract);
    }

    public ContractDetailDto terminateContract(UUID roomNumber, TerminateContractRequest request, UUID terminatedByUserId) {
        Specification<Contract> spec = ContractSpecification.hasUnitId(roomNumber).and(ContractSpecification.hasStatus(Contract.ContractStatus.active));
        Contract contract = contractRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (contract.getStatus() != Contract.ContractStatus.active) {
            throw new BadRequestException("Only active contracts can be terminated");
        }

        User terminatedByUser = userRepository.findById(terminatedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        contract.setStatus(Contract.ContractStatus.terminated);
        contract.setTerminationDate(LocalDate.now());
        contract.setTerminationReason(request.getTerminationReason());
        contract.setTerminatedByUser(terminatedByUser);

        Contract savedContract = contractRepository.save(contract);

        // Update unit status to available
        Unit unit = contract.getUnit();
        unit.setStatus(Unit.UnitStatus.available);
        unitRepository.save(unit);

        log.info("Contract terminated: {} for unit: {}", savedContract.getContractNumber(), unit.getUnitName());

        return contractMapper.toContractDetailDto(savedContract);
    }




}