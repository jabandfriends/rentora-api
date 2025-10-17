package com.rentora.api.service;


import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Utility.Request.UpdateUtilityDto;
import com.rentora.api.model.dto.Utility.Response.UtilitySummaryResponseDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.UtilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UtilityService {

    private final UtilityRepository utilityRepository;
    private final ApartmentRepository apartmentRepository;

    public List<UtilitySummaryResponseDto> getUtilityByApartmentId(UUID apartmentId) {
        List<Utility> utility = utilityRepository.findByApartmentId(apartmentId);

        return utility.stream().map(this::utilitySummaryResponseDto).toList();
    }

    //update utility with utility id and apartmentId
    public void updateUtilityByApartmentId(UUID apartmentId, UpdateUtilityDto request) {
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow(()-> new ResourceNotFoundException("Apartment not found"));

        Utility currentWaterUtility = utilityRepository.findByIdAndApartment(request.getWaterUtilityId(), apartment).orElseThrow(()-> new ResourceNotFoundException("Water Utility not found"));
        Utility currentElectricUtility = utilityRepository.findByIdAndApartment(request.getElectricUtilityId(),apartment).orElseThrow(()-> new ResourceNotFoundException("Electric Utility not found"));

        if(request.getElectricUtilityFixedPrice() != null) currentElectricUtility.setFixedPrice(request.getElectricUtilityFixedPrice());
        if(request.getElectricUtilityUnitPrice() != null) currentElectricUtility.setUnitPrice(request.getElectricUtilityUnitPrice());
        if(request.getElectricUtilityType() != null) currentElectricUtility.setUtilityType(request.getElectricUtilityType());

        if(request.getWaterUtilityFixedPrice() != null) currentWaterUtility.setFixedPrice(request.getWaterUtilityFixedPrice());
        if(request.getWaterUtilityUnitPrice() != null) currentWaterUtility.setUnitPrice(request.getWaterUtilityUnitPrice());
        if(request.getWaterUtilityType() != null) currentWaterUtility.setUtilityType(request.getWaterUtilityType());

        utilityRepository.saveAll(List.of(currentWaterUtility,currentElectricUtility));

    }

    private UtilitySummaryResponseDto utilitySummaryResponseDto(Utility utility) {
        return UtilitySummaryResponseDto.builder()
                .utilityId(utility.getId())
                .utilityName(utility.getUtilityName())
                .utilityType(utility.getUtilityType())
                .utilityFixedPrice(utility.getFixedPrice())
                .utilityUnitPrice(utility.getUnitPrice())
                .build();
    }
}
