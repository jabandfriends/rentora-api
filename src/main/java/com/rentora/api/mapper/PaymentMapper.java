package com.rentora.api.mapper;

import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponseDto toPaymentResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .verificationStatus(payment.getVerificationStatus())
                .amount(payment.getAmount())
                .tenantName(payment.getInvoice().getTenant().getFullName())
                .unitName(payment.getInvoice().getUnit().getUnitName())
                .buildingName(payment.getInvoice().getUnit().getFloor().getBuilding().getName())
                .floorName(payment.getInvoice().getUnit().getFloor().getFloorName())
                .build();
    }
}
