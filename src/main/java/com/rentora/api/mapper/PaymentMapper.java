package com.rentora.api.mapper;

import com.rentora.api.model.dto.Payment.Response.TenantPaymentsResponseDto;
import com.rentora.api.model.dto.Payment.Response.UpdatePaymentResponseDto;
import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Payment;
import com.rentora.api.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final S3FileService s3FileService;

    public TenantPaymentsResponseDto toTenantPayments(Payment payment) {
        URL paymentReceipt = null;
        if(payment.getReceiptUrl() !=null && !payment.getReceiptUrl().isEmpty()){
            paymentReceipt = s3FileService.generatePresignedUrlForGet(payment.getReceiptUrl());
        }
        return TenantPaymentsResponseDto.builder()
                .paymentId(payment.getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .verificationStatus(payment.getVerificationStatus())
                .paymentDueDate(payment.getInvoice().getDueDate())
                .paymentStatus(payment.getPaymentStatus())
                .paymentReceiptUrl(paymentReceipt)
                .paidDate(payment.getPaidAt())
                .paymentAmount(payment.getAmount())
                .build();
    }

    public PaymentResponseDto toPaymentResponseDto(Payment payment) {
        URL paymentReceipt = null;
        if(payment.getReceiptUrl() !=null && !payment.getReceiptUrl().isEmpty()){
            paymentReceipt = s3FileService.generatePresignedUrlForGet(payment.getReceiptUrl());
        }
        Invoice invoice = payment.getInvoice();
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .invoiceStatus(invoice.getPaymentStatus())
                .invoiceNumber(invoice.getInvoiceNumber())
                .paymentNumber(payment.getPaymentNumber())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .verificationStatus(payment.getVerificationStatus())
                .amount(payment.getAmount())
                .tenantName(payment.getInvoice().getTenant().getFullName())
                .unitName(payment.getInvoice().getUnit().getUnitName())
                .buildingName(payment.getInvoice().getUnit().getFloor().getBuilding().getName())
                .floorName(payment.getInvoice().getUnit().getFloor().getFloorName())
                .receiptUrl(paymentReceipt)
                .build();
    }

    public UpdatePaymentResponseDto toUpdatePaymentResponseDto(Payment payment, URL presignedURL) {
        return UpdatePaymentResponseDto.builder()
                .paymentId(payment.getId())
                .presignedURL(presignedURL)
                .build();
    }
}
