package com.rentora.api.model.dto.Contract.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ContractRoomServiceDto {
    private UUID id;
    private String serviceName;
    private BigDecimal servicePrice;
    private Boolean isActive;
}
