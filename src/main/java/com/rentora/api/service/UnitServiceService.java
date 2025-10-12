package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.UnitService.Request.CreateUnitServiceRequest;
import com.rentora.api.model.dto.UnitService.Response.ExecuteUnitServiceResponse;
import com.rentora.api.model.dto.UnitService.Response.UnitServiceInfoDTO;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.ServiceEntity;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitServiceEntity;
import com.rentora.api.repository.ApartmentServiceRepository;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitServiceService {

    private final UnitServiceRepository unitServiceRepository;
    private final UnitRepository unitRepository;
    private final ApartmentServiceRepository apartmentServiceRepository;

    public List<UnitServiceInfoDTO> getUnitServicesByUnit(UUID unitId) {

        // 1. ดึง List ของ UnitServiceEntity
        List<UnitServiceEntity> unitServices = unitServiceRepository.findAllByUnitId(unitId);

        if (unitServices.isEmpty()) {

            return Collections.emptyList();
        }
        return unitServices.stream()
                .map(this::toUnitServiceInfoDTO)
                .collect(Collectors.toList());
    }

    public ExecuteUnitServiceResponse createUnitService(UUID unitId, CreateUnitServiceRequest request) {

        Unit unit  = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + unitId));

        ServiceEntity service = apartmentServiceRepository.findById(request.getServiceId()) // สมมติว่า Request มี getServiceId()
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + request.getServiceId()));

        UnitServiceEntity unitServiceEntity = new  UnitServiceEntity();

        unitServiceEntity.setUnit(unit);
        unitServiceEntity.setServiceEntity(service);

        BigDecimal price = service.getPrice();
        unitServiceEntity.setMonthlyPrice(price);


        UnitServiceEntity savedUnitService = unitServiceRepository.save(unitServiceEntity);

        return new ExecuteUnitServiceResponse(savedUnitService.getId());

    }

    public void deleteUnitService(UUID unitServiceId) {
        UnitServiceEntity unitService = unitServiceRepository.findById(unitServiceId).orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: "));

        unitServiceRepository.delete(unitService);

        log.info("Unit Service with ID: " + unitServiceId + unitService.getServiceEntity().getServiceName() + " has been deleted");
    }


    private UnitServiceInfoDTO toUnitServiceInfoDTO(UnitServiceEntity unitServiceEntity) {

        String serviceName = unitServiceEntity.getServiceEntity().getServiceName();
        String unitName = unitServiceEntity.getUnit().getUnitName();

        UnitServiceInfoDTO unitServiceDTO = new UnitServiceInfoDTO();

        unitServiceDTO.setId(unitServiceEntity.getId());

        unitServiceDTO.setUnitName(unitName);

        unitServiceDTO.setServiceName(serviceName);

        unitServiceDTO.setPrice(unitServiceEntity.getMonthlyPrice());

        return unitServiceDTO;

    }

}
