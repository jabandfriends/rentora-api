package com.rentora.api.service;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Payment.Response.PaymentMonthlyAvenue;
import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.entity.Payment;
import com.rentora.api.repository.PaymentRepository;
import com.rentora.api.specifications.PaymentSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentService {
    private final PaymentRepository paymentRepository;

    //get all payment
    public List<PaymentResponseDto> getAllPayments(UUID apartmentId,String buildingName ,Payment.PaymentStatus paymentStatus) {
        Specification<Payment> specification = PaymentSpecification.hasPaymentStatus(paymentStatus).and(PaymentSpecification.hasApartment(apartmentId));
        List<Payment> payments = paymentRepository.findAll(specification);

        return payments.stream().map(this::toPaymentResponseDto).toList();
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

    public PaymentResponseDto toPaymentResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
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
