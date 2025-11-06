package com.rentora.api.service;


import com.rentora.api.mapper.PaymentMapper;
import com.rentora.api.model.dto.Payment.Request.UpdatePaymentRequestDto;
import com.rentora.api.model.dto.Payment.Response.UpdatePaymentResponseDto;
import com.rentora.api.model.dto.Payment.Response.PaymentMetadata;
import com.rentora.api.model.dto.Payment.Response.PaymentMonthlyAvenue;
import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.PaymentRepository;
import com.rentora.api.specifications.PaymentSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;




@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentService {
    private final S3FileService s3FileService;

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final AdhocInvoiceRepository  adhocInvoiceRepository;
    private final PaymentMapper paymentMapper;

    //get all payment
    public Page<PaymentResponseDto> getAllPayments(UUID apartmentId, String buildingName , Payment.PaymentStatus paymentStatus, Pageable pageable) {
        Specification<Payment> specification = PaymentSpecification.hasPaymentStatus(paymentStatus).and(PaymentSpecification.hasApartment(apartmentId))
                .and(PaymentSpecification.hasBuilding(buildingName));
        Page<Payment> payments = paymentRepository.findAll(specification,pageable);

        return payments.map(paymentMapper::toPaymentResponseDto);
    }

    //get metadata
    public PaymentMetadata getPaymentMetadata(UUID apartmentId) {
        long totalPayment = paymentRepository.countPaymentByApartment(apartmentId);
        long totalPaymentComplete = paymentRepository.countPaymentByApartmentIdAndStatus(apartmentId, Payment.PaymentStatus.completed);
        long totalPaymentPending = paymentRepository.countPaymentByApartmentIdAndStatus(apartmentId, Payment.PaymentStatus.pending);
        long totalPaymentFailed = paymentRepository.countPaymentByApartmentIdAndStatus(apartmentId, Payment.PaymentStatus.failed);

        return PaymentMetadata.builder()
                .totalPayments(totalPayment)
                .totalPaymentsComplete(totalPaymentComplete)
                .totalPaymentsPending(totalPaymentPending)
                .totalPaymentsFailed(totalPaymentFailed)
                .build();
    }

    //update payment
    public UpdatePaymentResponseDto updatePayment(UUID paymentId,UpdatePaymentRequestDto request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Apartment currentApartment = payment.getInvoice().getApartment();
        String logoImgKey = null;
        URL presignedUrl = null;
        if(request.getReceiptFilename() != null && !request.getReceiptFilename().isEmpty()) {
            if(payment.getReceiptUrl() != null && !payment.getReceiptUrl().isEmpty()) {
                s3FileService.deleteFile(payment.getReceiptUrl());
            }
            logoImgKey = "apartments/payment/"+currentApartment.getId() + LocalDateTime.now() + "-" + request.getReceiptFilename();
            try {
                presignedUrl = s3FileService.generatePresignedUrlForPut(logoImgKey);
                payment.setReceiptUrl(logoImgKey);
            } catch (Exception e) {
                log.warn("Failed to generate presigned PUT URL for apartment logo: {}", e.getMessage());
            }
        }

        if(request.getVerificationStatus() != null){
            payment.setVerificationStatus(request.getVerificationStatus());
        }

        Invoice invoice = payment.getInvoice();
        Unit unit = payment.getInvoice().getUnit();
        List<AdhocInvoice> adhocInvoices = adhocInvoiceRepository.findByUnit(unit)
                .stream().filter(AdhocInvoice::getIncludeInMonthly)
                .filter(adhoc -> adhoc.getPaymentStatus() == AdhocInvoice.PaymentStatus.unpaid)
                .toList();

        if(request.getPaymentStatus() != null){
            if(request.getPaymentStatus().equals(Payment.PaymentStatus.completed)){
                invoice.setPaymentStatus(Invoice.PaymentStatus.paid);
                adhocInvoices.forEach(adhocInvoice -> adhocInvoice.setPaymentStatus(AdhocInvoice.PaymentStatus.paid));
            }else{
                invoice.setPaymentStatus(Invoice.PaymentStatus.unpaid);
                adhocInvoices.forEach(adhocInvoice -> adhocInvoice.setPaymentStatus(AdhocInvoice.PaymentStatus.unpaid));
            }
            payment.setPaymentStatus(request.getPaymentStatus());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toUpdatePaymentResponseDto(updatedPayment,presignedUrl);


    }

    public PaymentMonthlyAvenue getMonthlyData(UUID apartmentId) {
        // 🗓️ Get current date and month automatically
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1); // first day of current month
        LocalDate next = start.plusMonths(1);    // first day of next month

        OffsetDateTime startOfMonth = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime startOfNextMonth = next.atStartOfDay().atOffset(ZoneOffset.UTC);

        // 💰 Fetch data using your queries
        BigDecimal monthly = paymentRepository.getMonthlyRevenueByApartment(apartmentId, startOfMonth, startOfNextMonth);
        BigDecimal total = paymentRepository.getTotalRevenueByApartment(apartmentId);
        BigDecimal pending = paymentRepository.getPendingPaymentByApartment(apartmentId);

        return PaymentMonthlyAvenue.builder()
                .monthlyRevenue(monthly)
                .totalRevenue(total)
                .pendingPayment(pending)
                .build();
    }


}
