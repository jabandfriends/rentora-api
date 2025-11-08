package com.rentora.api.service;


import com.rentora.api.mapper.PaymentMapper;
import com.rentora.api.model.dto.Payment.Request.UpdatePaymentRequestDto;
import com.rentora.api.model.dto.Payment.Response.UpdatePaymentResponseDto;
import com.rentora.api.model.dto.Payment.Response.PaymentMetadata;
import com.rentora.api.model.dto.Payment.Response.PaymentMonthlyAvenue;
import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.*;
import com.rentora.api.specifications.MonthlyInvoiceSpecification;
import com.rentora.api.specifications.PaymentSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;



@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentService {
    private final S3FileService s3FileService;

    private final PaymentRepository paymentRepository;
    private final AdhocInvoiceRepository  adhocInvoiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    private final PaymentMapper paymentMapper;

    //get all payment
    public Page<PaymentResponseDto> getAllPayments(LocalDate genMonth,UUID apartmentId, String buildingName , Payment.PaymentStatus paymentStatus, Pageable pageable) {
        Specification<Payment> specification = PaymentSpecification.hasPaymentStatus(paymentStatus).and(PaymentSpecification.hasApartment(apartmentId))
                .and(PaymentSpecification.hasBuilding(buildingName)).and(PaymentSpecification.matchGenerationDate(genMonth));
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
    public UpdatePaymentResponseDto updatePayment(UUID currentUserId,UUID paymentId,UpdatePaymentRequestDto request) {
        //payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        //monthly invoice
        Invoice invoice = payment.getInvoice();

        //current user
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));

        //apartment
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
            payment.setVerifiedByUser(user);
            payment.setVerifiedAt(OffsetDateTime.now());
            payment.setPaidAt(OffsetDateTime.now());
        }


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

    //payment apply late fee
    // Run at 2:00 AM every day
    @Scheduled(cron = "0 0 2 * * *")
    public void applyLateFee(){
        //get unpaid
        Specification<Invoice> specification = MonthlyInvoiceSpecification.hasPaymentStatus(Invoice.PaymentStatus.unpaid);
        List<Invoice> invoices = invoiceRepository.findAll(specification);

        //update status to late
        for(Invoice invoice: invoices){
            Apartment currentApartment = invoice.getApartment();
            //check late bill
            LocalDate graceEndDate = invoice.getDueDate().plusDays(currentApartment.getGracePeriodDays());
            //payment
            Payment payment = paymentRepository.findByInvoice(invoice).orElse(null);
            if (payment == null) continue;

            long overdueDays = ChronoUnit.DAYS.between(graceEndDate, LocalDate.now());
            if(overdueDays > 0){
                //update status to late both payment and invoice , payment
                invoice.setPaymentStatus(Invoice.PaymentStatus.overdue);

                BigDecimal currentAmount = payment.getAmount();
                //check apartment setting type first
                Apartment.LateFeeType lateFeeType = currentApartment.getLateFeeType();
                if(lateFeeType.equals(Apartment.LateFeeType.fixed)){
                    BigDecimal lateFee = currentApartment.getLateFee();
                    currentAmount = currentAmount.add(lateFee);
                    payment.setAmount(currentAmount);
                }else{
                    BigDecimal lateFeePercentage =  currentApartment.getLateFee();
                    BigDecimal totalAdded =  currentAmount.multiply(lateFeePercentage).divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);
                    currentAmount = currentAmount.add(totalAdded);
                    payment.setAmount(currentAmount);
                }

            }
        }
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
