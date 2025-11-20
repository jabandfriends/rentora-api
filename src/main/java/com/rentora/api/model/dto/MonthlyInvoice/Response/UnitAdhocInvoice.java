package com.rentora.api.model.dto.MonthlyInvoice.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UnitAdhocInvoice {
    private UUID adhocId;
    private String adhocNumber;
    private String adhocTitle;
    private BigDecimal amount;
}
