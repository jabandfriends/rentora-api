package com.rentora.api.model.dto.MonthlyInvoice.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UnitServiceList {
    private String serviceName;
    private BigDecimal servicePrice;
}
