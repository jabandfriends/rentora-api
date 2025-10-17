package com.rentora.api.service;

import com.rentora.api.model.dto.UnitService.Response.UnitServiceInfoDTO;
import com.rentora.api.model.entity.UnitServiceEntity;
import com.rentora.api.repository.UnitServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitServiceService {

    private final UnitServiceRepository unitServiceRepository;

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
