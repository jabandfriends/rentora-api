package com.rentora.api.model.dto.MonthlyInvoice.Response;

import com.rentora.api.model.entity.Invoice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MonthlyInvoiceResponseDto {
    private UUID invoiceId;
    private String unitName;
    private String buildingName;
    private String tenantName;
    private BigDecimal totalAmount;
    private Invoice.PaymentStatus paymentStatus;
}
