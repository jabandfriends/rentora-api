package com.rentora.api.service;

import com.rentora.api.dto.Apartment.Request.CreateApartmentRequest;
import com.rentora.api.dto.Apartment.Request.UpdateApartmentRequest;
import com.rentora.api.dto.Apartment.Response.ApartmentDetailDTO;
import com.rentora.api.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.entity.Apartment;
import com.rentora.api.entity.ApartmentUser;
import com.rentora.api.entity.User;
import com.rentora.api.enums.UserRole;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


    private final ContractRepository contractRepository;
    private final ApartmentUserRepository apartmentUserRepository;

    public Page<ApartmentSummaryDTO> getApartments(UUID userId, String search, Pageable pageable) {
        Page<Apartment> apartments;


        if (search != null && !search.trim().isEmpty()) {

            apartments = apartmentRepository.findByUserIdAndNameContaining(userId, search.trim(), pageable);
        } else {

            apartments = apartmentRepository.findByUserId(userId, pageable);
        }

        return apartments.map(this::toApartmentSummaryDto);
    }

    public ApartmentDetailDTO getApartmentById(UUID apartmentId, UUID userId) {
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        return toApartmentDetailDto(apartment);
    }

    public ApartmentDetailDTO createApartment(CreateApartmentRequest request, UUID createdByUserId) {
        User createdByUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Apartment apartment = new Apartment();
        apartment.setName(request.getName());
        apartment.setLogoUrl(request.getLogoUrl());
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

        // Create apartment user relationship with admin role
        ApartmentUser apartmentUser = new ApartmentUser();
        apartmentUser.setApartment(savedApartment);
        apartmentUser.setUser(createdByUser);
        apartmentUser.setRole(UserRole.admin);
        apartmentUser.setIsActive(true);
        apartmentUser.setCreatedBy(createdByUser);
        apartmentUserRepository.save(apartmentUser);

        log.info("Apartment created: {} by user: {}", savedApartment.getName(), createdByUser.getEmail());

        return toApartmentDetailDto(savedApartment);
    }

    public ApartmentDetailDTO updateApartment(UUID apartmentId, UpdateApartmentRequest request, UUID userId) {
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

        Apartment savedApartment = apartmentRepository.save(apartment);

        log.info("Apartment updated: {}", savedApartment.getName());

        return toApartmentDetailDto(savedApartment);
    }

    public void deleteApartment(UUID apartmentId, UUID userId) {
        Apartment apartment = apartmentRepository.findByIdAndUserId(apartmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found or access denied"));

        // Check if apartment has active contracts
        long activeContracts = contractRepository.countActiveByApartmentId(apartmentId);
        if (activeContracts > 0) {
            throw new BadRequestException("Cannot delete apartment with active contracts");
        }

        apartmentRepository.delete(apartment);

        log.info("Apartment deleted: {}", apartment.getName());
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
