package com.rentora.api.model.dto.UnitService.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UnitServiceInfoDTO {
    private UUID id;
    private String UnitName;
//    private String ServiceEntity;
    private String ServiceName;
    private BigDecimal Price;
}
