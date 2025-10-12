package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.model.entity.ServiceEntity;
import com.rentora.api.repository.ApartmentServiceRepository;
import com.rentora.api.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentServiceService {

    private final UnitRepository unitRepository;
    private final ApartmentServiceRepository serviceRepository;

    public List<ServiceInfoDTO> getApartmentService(UUID apartmentId) {

        List<ServiceEntity> serviceEntities = serviceRepository.findAllByApartmentId(apartmentId);

        if (serviceEntities.isEmpty()) {
            throw new ResourceNotFoundException("No Services found for Apartment ID: " + apartmentId);
        }

        return serviceEntities.stream()
                .map(this::toServiceInfoDTO)
                .collect(Collectors.toList());
    }

    private ServiceInfoDTO toServiceInfoDTO(ServiceEntity services) {
        ServiceInfoDTO dto = new ServiceInfoDTO();
        dto.setId(services.getId());
        dto.setServiceName(services.getServiceName());
        dto.setPrice(services.getPrice());

        return dto;
    }
}
