package com.rentora.api.service;

import com.rentora.api.model.dto.Apartment.Metadata.ApartmentMetadataDto;
import com.rentora.api.model.dto.Apartment.Request.CreateApartmentRequest;
import com.rentora.api.model.dto.Apartment.Request.SetupApartmentRequest;
import com.rentora.api.model.dto.Apartment.Request.UpdateApartmentRequest;
import com.rentora.api.model.dto.Apartment.Response.ApartmentDetailDTO;

import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.Apartment.Response.ExecuteApartmentResponse;
import com.rentora.api.model.entity.*;
import com.rentora.api.constant.enums.UserRole;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.*;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.ApartmentSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentService {


    private final ApartmentRepository apartmentRepository;

    private final UserRepository userRepository;

    private final BuildingRepository buildingRepository;

    private final UnitRepository unitRepository;

    private final S3FileService s3FileService;

    private final ContractRepository contractRepository;

    private final ApartmentUserRepository apartmentUserRepository;

    private final ApartmentServiceRepository serviceRepository;

    private final UtilityRepository utilityRepository;

    private final ApartmentPaymentRepository apartmentPaymentRepository;

    private final FloorRepository floorRepository;

    public Page<ApartmentSummaryDTO> getApartments(UUID userId, String search, Apartment.ApartmentStatus status, Pageable pageable) {


        Specification<Apartment> spec = ApartmentSpecification.hasUserId(userId).and(ApartmentSpecification.hasName(search)).and(ApartmentSpecification.hasStatus(status));
        Page<Apartment> apartments = apartmentRepository.findAll(spec, pageable);

        return apartments.map(apartment -> {
            ApartmentSummaryDTO dto = toApartmentSummaryDto(apartment);

            // Generate GET presigned URL for download if logo exists
            if (apartment.getLogoUrl() != null && !apartment.getLogoUrl().isBlank()) {
                try {
                    URL presignedUrl = s3FileService.generatePresignedUrlForGet(apartment.getLogoUrl());
                    dto.setLogoPresignedUrl(presignedUrl.toString());
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for apartment logo: {}", e.getMessage());
                }
            }

            return dto;
        });
    }
    public ApartmentMetadataDto getApartmentsMetadata(List<ApartmentSummaryDTO> apartments) {
        ApartmentMetadataDto apartmentMetadataResponse = new ApartmentMetadataDto();
        apartmentMetadataResponse.setTotalApartments(apartments.size());
        long totalActiveApartments = apartments.stream()
                .filter(apartment -> apartment.getStatus() == Apartment.ApartmentStatus.active)
                .count();
        apartmentMetadataResponse.setTotalActiveApartments(totalActiveApartments);
        return apartmentMetadataResponse;
    }

    public ApartmentDetailDTO getApartmentById(UUID apartmentId, UUID userId) {
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        ApartmentDetailDTO dto = toApartmentDetailDto(apartment);

        if (apartment.getLogoUrl() != null && !apartment.getLogoUrl().isBlank()) {
            try {
                URL presignedUrl = s3FileService.generatePresignedUrlForGet(apartment.getLogoUrl());
                dto.setLogoPresignedUrl(presignedUrl.toString());
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for apartment logo: {}", e.getMessage());
            }
        }

        return dto;
    }


    public ExecuteApartmentResponse createApartment(CreateApartmentRequest request, UUID createdByUserId) {
        User createdByUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String logoImgKey = null;
        String presignedUrlStr = null;

        if (request.getLogoFileName() != null) {
            logoImgKey = "apartments/logo/" + UUID.randomUUID() + "-" + request.getLogoFileName();
            try {
                URL presignedUrl = s3FileService.generatePresignedUrlForPut(logoImgKey);
                presignedUrlStr = presignedUrl.toString();
            } catch (Exception e) {
                log.warn("Failed to generate presigned PUT URL for apartment logo: {}", e.getMessage());
            }
        }

        Apartment apartment = new Apartment();
        apartment.setName(request.getName());
        apartment.setLogoUrl(logoImgKey);
        apartment.setPhoneNumber(request.getPhoneNumber());
        apartment.setTaxId(request.getTaxId());
        apartment.setPaymentDueDay(request.getPaymentDueDay());
        apartment.setLateFee(request.getLateFee());
        apartment.setLateFeeType(request.getLateFeeType());
        apartment.setGracePeriodDays(request.getGracePeriodDays());
        apartment.setAddress(request.getAddress());
        apartment.setCity(request.getCity());
        apartment.setState(request.getState());
        apartment.setPostalCode(request.getPostalCode());
        apartment.setCountry(request.getCountry());
        apartment.setTimezone(request.getTimezone());
        apartment.setCurrency(request.getCurrency());
        apartment.setCreatedBy(createdByUser);
        apartment.setStatus(Apartment.ApartmentStatus.setup_incomplete);

        Apartment savedApartment = apartmentRepository.save(apartment);

        ApartmentUser apartmentUser = new ApartmentUser();
        apartmentUser.setApartment(savedApartment);
        apartmentUser.setUser(createdByUser);
        apartmentUser.setRole(UserRole.admin);
        apartmentUser.setIsActive(true);
        apartmentUser.setCreatedBy(createdByUser);
        apartmentUserRepository.save(apartmentUser);

        log.info("Apartment created: {} by user: {}", savedApartment.getName(), createdByUser.getEmail());

        return new ExecuteApartmentResponse(apartment.getId(), presignedUrlStr, logoImgKey);
    }

    public ExecuteApartmentResponse updateApartment(UUID apartmentId, UpdateApartmentRequest request, UUID userId) {
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        if (request.getName() != null) apartment.setName(request.getName());
        if (request.getLogoUrl() != null) apartment.setLogoUrl(request.getLogoUrl());
        if (request.getPhoneNumber() != null) apartment.setPhoneNumber(request.getPhoneNumber());
        if (request.getTaxId() != null) apartment.setTaxId(request.getTaxId());
        if (request.getPaymentDueDay() != null) apartment.setPaymentDueDay(request.getPaymentDueDay());
        if (request.getLateFee() != null) apartment.setLateFee(request.getLateFee());
        if (request.getLateFeeType() != null) apartment.setLateFeeType(request.getLateFeeType());
        if (request.getGracePeriodDays() != null) apartment.setGracePeriodDays(request.getGracePeriodDays());
        if (request.getAddress() != null) apartment.setAddress(request.getAddress());
        if (request.getCity() != null) apartment.setCity(request.getCity());
        if (request.getState() != null) apartment.setState(request.getState());
        if (request.getPostalCode() != null) apartment.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) apartment.setCountry(request.getCountry());
        if (request.getTimezone() != null) apartment.setTimezone(request.getTimezone());
        if (request.getCurrency() != null) apartment.setCurrency(request.getCurrency());

        String logoImgKey = null;
        String presignedUrlStr = null;
        if (request.getLogoFileName() != null) {
            logoImgKey = "apartments/logo/" + UUID.randomUUID() + "-" + request.getLogoFileName();
            try {
                URL presignedUrl = s3FileService.generatePresignedUrlForPut(logoImgKey);
                presignedUrlStr = presignedUrl.toString();
            } catch (Exception e) {
                log.warn("Failed to generate presigned PUT URL for apartment logo: {}", e.getMessage());
            }
        }
        Apartment savedApartment = apartmentRepository.save(apartment);

        log.info("Apartment updated: {}", savedApartment.getName());

        return new ExecuteApartmentResponse(apartment.getId(),presignedUrlStr,logoImgKey);
    }

    public void deleteApartment(UUID apartmentId, UUID userId) {
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        // Check if apartment has active contracts
        long activeContracts = contractRepository.countActiveByApartmentId(apartmentId);
        if (activeContracts > 0) {
            throw new BadRequestException("Cannot delete apartment with active contracts");
        }

        if (apartment.getLogoUrl() != null && !apartment.getLogoUrl().isBlank()) {
            try {
                s3FileService.deleteFile(apartment.getLogoUrl());
            } catch (Exception e) {
                log.warn("Failed to delete apartment logo from S3: {}", e.getMessage());
            }
        }

        apartmentRepository.delete(apartment);

        log.info("Apartment deleted: {}", apartment.getName());
    }
    //setup
    public void apartmentSetup(UUID apartmentId, SetupApartmentRequest request, UUID userId) {
        User createdByUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //check apartment first
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        //save service
        request.getServices().forEach(serviceItem -> {
            ServiceEntity service = new ServiceEntity();
            //save apartment fk first
            service.setApartment(apartment);
            service.setServiceName(serviceItem.getName());
            service.setPrice(serviceItem.getPrice());
            serviceRepository.save(service);
        });

        //apartment water utility
        Utility waterUtility = new Utility();
        waterUtility.setApartment(apartment);
        waterUtility.setUtilityName("water");
        waterUtility.setUtilityType(request.getWaterType());
        waterUtility.setCategory(Utility.Category.utility);
        waterUtility.setUnitPrice(request.getWaterPrice());
        waterUtility.setFixedPrice(request.getWaterFlat());
        utilityRepository.save(waterUtility);

        //apartment electric utility
        Utility electricityUtility = new Utility();
        electricityUtility.setApartment(apartment);
        electricityUtility.setUtilityName("electric");
        electricityUtility.setUtilityType(request.getElectricityType());
        electricityUtility.setCategory(Utility.Category.utility);
        electricityUtility.setUnitPrice(request.getElectricityPrice());
        electricityUtility.setFixedPrice(request.getElectricityFlat());
        utilityRepository.save(electricityUtility);

        //save building
        request.getBuildings().forEach(buildingItem -> {
            Building building = new Building();
            building.setApartment(apartment);
            building.setName(buildingItem.getBuildingName());
            building.setTotalFloors(buildingItem.getTotalFloors());
            buildingRepository.save(building);


            Integer totalFloors = buildingItem.getTotalFloors();
            //save floors
            for (Integer i = 1; i <= totalFloors; i++) {
                Floor floor = new Floor();
                floor.setBuilding(building);
                floor.setFloorNumber(i);
                floor.setFloorName("Floor " + i);
                floor.setTotalUnits(buildingItem.getTotalUnitPerFloor());
                floorRepository.save(floor);

                //generate unit
                int totalUnits = buildingItem.getTotalUnitPerFloor();
                List<Unit> units = new ArrayList<>();

                for (int j = 1; j <= totalUnits; j++) {
                    Unit unit = new Unit();
                    unit.setFloor(floor);
                    // Generate name like ROOM101, ROOM102, etc.
                    String unitName = String.format("ROOM%d%02d", i, j);
                    unit.setUnitName(unitName);
                    unit.setStatus(Unit.UnitStatus.available);
                    units.add(unit);
                }

                unitRepository.saveAll(units);

            }

        });

        //payment
        ApartmentPayment payment = new ApartmentPayment();
        payment.setApartment(apartment);
        payment.setMethodName(ApartmentPayment.MethodType.bank_transfer);
        payment.setBankName(request.getBankName());
        payment.setBankAccountNumber(request.getBankAccountNumber());
        payment.setAccountHolderName(request.getBankAccountHolder());
        payment.setCreatedBy(createdByUser);
        apartmentPaymentRepository.save(payment);

        // activate apartment
        apartment.setStatus(Apartment.ApartmentStatus.active);
        apartmentRepository.save(apartment);

        log.info("Apartment {} create a service successfully", apartment.getName());
    }

    private ApartmentSummaryDTO toApartmentSummaryDto(Apartment apartment) {
        ApartmentSummaryDTO dto = new ApartmentSummaryDTO();
        dto.setId(apartment.getId().toString());
        dto.setName(apartment.getName());
        dto.setLogoUrl(apartment.getLogoUrl());
        dto.setPhoneNumber(apartment.getPhoneNumber());
        dto.setAddress(apartment.getAddress());
        dto.setCity(apartment.getCity());
        dto.setState(apartment.getState());
        dto.setStatus(apartment.getStatus());
        dto.setCreatedAt(apartment.getCreatedAt() != null ? apartment.getCreatedAt().toString() : null);
        dto.setUpdatedAt(apartment.getUpdatedAt() != null ? apartment.getUpdatedAt().toString() : null);

        // Get counts
        dto.setBuildingCount(buildingRepository.countByApartmentId(apartment.getId()));
        dto.setUnitCount(unitRepository.countByApartmentId(apartment.getId()));
        dto.setActiveContractCount(contractRepository.countActiveByApartmentId(apartment.getId()));

        return dto;
    }

    private ApartmentDetailDTO toApartmentDetailDto(Apartment apartment) {
        ApartmentDetailDTO dto = new ApartmentDetailDTO();
        dto.setId(apartment.getId().toString());
        dto.setName(apartment.getName());
        dto.setLogoUrl(apartment.getLogoUrl());
        dto.setPhoneNumber(apartment.getPhoneNumber());
        dto.setTaxId(apartment.getTaxId());
        dto.setPaymentDueDay(apartment.getPaymentDueDay());
        dto.setLateFee(apartment.getLateFee());
        dto.setLateFeeType(apartment.getLateFeeType());
        dto.setGracePeriodDays(apartment.getGracePeriodDays());
        dto.setAddress(apartment.getAddress());
        dto.setCity(apartment.getCity());
        dto.setState(apartment.getState());
        dto.setPostalCode(apartment.getPostalCode());
        dto.setCountry(apartment.getCountry());
        dto.setTimezone(apartment.getTimezone());
        dto.setCurrency(apartment.getCurrency());
        dto.setStatus(apartment.getStatus());
        dto.setCreatedAt(apartment.getCreatedAt() != null ? apartment.getCreatedAt().toString() : null);
        dto.setUpdatedAt(apartment.getUpdatedAt() != null ? apartment.getUpdatedAt().toString() : null);

        // Get statistics
        dto.setBuildingCount(buildingRepository.countByApartmentId(apartment.getId()));
        dto.setUnitCount(unitRepository.countByApartmentId(apartment.getId()));
        dto.setActiveContractCount(contractRepository.countActiveByApartmentId(apartment.getId()));
        dto.setTotalTenants(contractRepository.countActiveByApartmentId(apartment.getId()));

        return dto;
    }


}
