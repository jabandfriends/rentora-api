package com.rentora.api.model.dto.Payment.Response;

import com.rentora.api.model.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class TenantPaymentsResponseDto {
    private UUID paymentId;
    private String invoiceNumber;
    private LocalDate paymentDueDate;
    private Payment.PaymentStatus paymentStatus;
    private Payment.VerificationStatus verificationStatus;
    private URL paymentReceiptUrl;
    private BigDecimal paymentAmount;
    private OffsetDateTime paidDate;
}
