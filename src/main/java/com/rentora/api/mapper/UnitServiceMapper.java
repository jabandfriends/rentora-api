package com.rentora.api.mapper;

import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitServiceList;
import com.rentora.api.model.entity.UnitServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class UnitServiceMapper {
    public UnitServiceList toUnitServiceList(UnitServiceEntity unitServiceEntities) {
        return UnitServiceList.builder()
                .serviceName(unitServiceEntities.getApartmentService().getServiceName())
                .servicePrice(unitServiceEntities.getApartmentService().getPrice())
                .build();
    }
}
