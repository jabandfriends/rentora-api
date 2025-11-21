package com.rentora.api.mapper;

import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.model.entity.ApartmentService;
import org.springframework.stereotype.Component;

@Component
public class ApartmentServiceMapper {
    public ServiceInfoDTO toServiceInfoDTO(ApartmentService services) {
        ServiceInfoDTO dto = new ServiceInfoDTO();
        dto.setId(services.getId());
        dto.setServiceName(services.getServiceName());
        dto.setPrice(services.getPrice());
        dto.setCategory(services.getCategory());
        dto.setIsActive(services.getIsActive());

        return dto;
    }
}
