package com.rentora.api.model.dto.Unit.Response;

import com.rentora.api.model.entity.Unit;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UnitWithUtilityAndMonthlyInvoiceStatus {
    private UUID unitId;
    private String unitName;
    private String buildingName;
    private Unit.UnitStatus unitStatus;
    private Boolean isMonthlyInvoiceCreated;
}
