package com.rentora.api.model.dto.Payment.Request;

import com.rentora.api.model.entity.Payment;
import lombok.Data;

@Data
public class UpdatePaymentRequestDto {
    private String receiptFilename;
    private Payment.VerificationStatus verificationStatus;
    private Payment.PaymentStatus paymentStatus;
}
