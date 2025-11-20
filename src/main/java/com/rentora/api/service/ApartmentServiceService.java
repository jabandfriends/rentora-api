package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.mapper.ApartmentServiceMapper;
import com.rentora.api.model.dto.ApartmentService.Request.ApartmentServiceCreateRequestDto;
import com.rentora.api.model.dto.ApartmentService.Request.ApartmentServiceUpdateRequestDto;
import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentService;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.ApartmentServiceRepository;
import com.rentora.api.specifications.ApartmentServiceSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentServiceService {

    private final ApartmentServiceRepository apartmentServiceRepository;
    private final ApartmentRepository apartmentRepository;

    private final ApartmentServiceMapper apartmentServiceMapper;


    public List<ServiceInfoDTO> getApartmentService(UUID apartmentId,Boolean isActive) {
        Specification<ApartmentService> specification = ApartmentServiceSpecification.hasApartmentId(apartmentId)
                .and(ApartmentServiceSpecification.isActive(isActive));
        List<ApartmentService> apartmentServices = apartmentServiceRepository.findAll(specification);

        if (apartmentServices.isEmpty()) {
            throw new ResourceNotFoundException("No Services found for Apartment ID: " + apartmentId);
        }

        return apartmentServices.stream()
                .map(apartmentServiceMapper::toServiceInfoDTO)
                .collect(Collectors.toList());
    }

    //create new apartment service
    public void createApartmentService(UUID apartmentId, ApartmentServiceCreateRequestDto request){
        //check existing name
        Optional<ApartmentService> apartmentService = apartmentServiceRepository.findByServiceName(request.getServiceName());
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow(()->new ResourceNotFoundException("Apartment not found with"));
        if(apartmentService.isPresent()){
            throw new BadRequestException("ApartmentService already exists");
        }

        ApartmentService newApartmentService = new ApartmentService();
        newApartmentService.setServiceName(request.getServiceName());
        newApartmentService.setPrice(request.getPrice());
        newApartmentService.setApartment(apartment);
        newApartmentService.setCategory(request.getCategory());
        apartmentServiceRepository.save(newApartmentService);

    }

    //update apartment service
    public void updateApartmentService(ApartmentServiceUpdateRequestDto request) {
        //check if it exist
        ApartmentService apartmentService = apartmentServiceRepository.findById(request.getApartmentServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if(request.getCategory() != null) apartmentService.setCategory(request.getCategory());
        if(request.getPrice() != null) apartmentService.setPrice(request.getPrice());
        if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
            apartmentService.setServiceName(request.getServiceName());
        }
        if(request.getIsActive()!= null) apartmentService.setIsActive(request.getIsActive());
        apartmentServiceRepository.save(apartmentService);
    }




}
