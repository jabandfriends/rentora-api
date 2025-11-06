package com.rentora.api.model.dto.Payment.Response;

import com.rentora.api.model.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URL;
import java.util.UUID;

@Data
@Builder
public class PaymentResponseDto {
    private UUID paymentId;
    private String paymentNumber;
    private String  paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private Payment.VerificationStatus verificationStatus;
    private BigDecimal amount;
    private String tenantName;
    private String unitName;
    private String buildingName;
    private String floorName;
    private URL receiptUrl;
}
