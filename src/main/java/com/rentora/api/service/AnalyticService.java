package com.rentora.api.service;

import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceCategorySummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceSummaryDTO;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceYearlySummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceYearlyTableSummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Payment.*;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Payment;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.repository.PaymentRepository;
import com.rentora.api.specifications.MaintenanceSpecification;
import com.rentora.api.specifications.PaymentSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AnalyticService {
    private final PaymentRepository paymentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AdhocInvoiceRepository adhocInvoiceRepository;

    //count maintenance category
    public List<MaintenanceCategorySummaryDto> maintenanceCategorySummary(UUID apartmentId){
        return maintenanceRepository.countMaintenanceByCategory(apartmentId)
                .stream()
                .map(p -> MaintenanceCategorySummaryDto.builder()
                        .category(p.getCategory())
                        .count(p.getCount())
                        .build())
                .toList();
    }
    public List<MaintenanceYearlyTableSummaryDto> maintenanceYearlyTableSummary(UUID apartmentId) {
        return maintenanceRepository.getYearlySummaryTable(apartmentId).stream()
                .map(p -> {
                    BigDecimal avgCost = p.getTotalRequests() > 0
                            ? p.getTotalCost().divide(BigDecimal.valueOf(p.getTotalRequests()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    double completionRate = p.getTotalRequests() > 0
                            ? (p.getCompleted() * 100.0 / p.getTotalRequests())
                            : 0.0;
                    return new MaintenanceYearlyTableSummaryDto(
                            p.getYear(),
                            p.getTotalRequests(),
                            p.getTotalCost(),
                            p.getCompleted(),
                            p.getPending(),
                            avgCost,
                            completionRate
                    );
                })
                .toList();
    }

    //get maintenance available year list
    public List<Integer> getAvailableYearList(UUID apartmentId){
        return maintenanceRepository.getAvailableYears(apartmentId);
    }
    public List<MaintenanceSummaryDTO> getMonthlySummary(int year,UUID apartmentId) {
        return maintenanceRepository.getMonthlySummary(year,apartmentId)
                .stream()
                .map(result -> new MaintenanceSummaryDTO(
                        Month.of(result.getMonth()).name(), // convert number → month name
                        result.getCount(),
                        result.getTotalCost() != null ? result.getTotalCost() : BigDecimal.ZERO
                ))
                .toList();
    }

    public List<MaintenanceYearlySummaryDto> getYearlySummary(UUID apartmentId) {
        return maintenanceRepository.getYearlySummary(apartmentId)
                .stream()
                .map(result -> new MaintenanceYearlySummaryDto(
                        result.getYear(),
                        result.getCount(),
                        result.getTotalCost() != null ? result.getTotalCost() : BigDecimal.ZERO
                ))
                .toList();
    }

    //------------------payment---------------------

    //analytic stats
    public PaymentStatsSummaryDto paymentStatsSummary(UUID apartmentId) {
        Specification<Payment> paymentSpecification = PaymentSpecification.hasApartment(apartmentId);
        List<Payment> payments = paymentRepository.findAll(paymentSpecification);
        BigDecimal totalRental = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal totalPaid = payments.stream().filter(payment->
                payment.getInvoice().getPaymentStatus().equals(Invoice.PaymentStatus.paid)
        ).map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal totalPending = payments.stream().filter(payment->
                payment.getInvoice().getPaymentStatus().equals(Invoice.PaymentStatus.unpaid)
        ).map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal totalOverdue = payments.stream().filter(payment->
                payment.getInvoice().getPaymentStatus().equals(Invoice.PaymentStatus.overdue)
        ).map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        return PaymentStatsSummaryDto.builder()
                .totalRental(totalRental)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .totalOverdue(totalOverdue)
                .build();
    }




    //available year for payment
    public List<Integer> getAvailableYearsPayment(UUID apartmentId){
        return  paymentRepository.getAvailableYears(apartmentId);
    }

    //revenue trends ( payment )
    public List<PaymentMonthlySummaryDto> getMonthlySummaryPayment(int year,UUID apartmentId){
        List<PaymentMonthlySummaryDto> response = paymentRepository.monthlySummaryByYear(year,apartmentId).stream()
                .map(result-> new PaymentMonthlySummaryDto(
                        Month.of(result.getMonth()).name(),
                        result.getCount(),
                        result.getTotalCost()
                )).toList();
        return response;
    }

    public List<PaymentYearlySummaryDto> getYearlySummaryPayment(UUID apartmentId){
        List<PaymentYearlySummaryDto> response = paymentRepository.yearlySummary(apartmentId).stream()
                .map(item-> new PaymentYearlySummaryDto(
                        item.getYear(), item.getCount(), item.getTotalCost()
                        )
                ).toList();
        return response;
    }

    //payment status distribution current month
    public List<PaymentDistributionSummaryDto> paymentDistributionSummaryDto(UUID apartmentId) {
        // Get all payments for the apartment
        Specification<Payment> paymentSpecification = PaymentSpecification.hasApartment(apartmentId);
        List<Payment> payments = paymentRepository.findAll(paymentSpecification);

        // Total number of payments
        int total = payments.size();
        if (total == 0) return Collections.emptyList(); // avoid division by zero

        // Group by PaymentStatus and count
        Map<Invoice.PaymentStatus, Long> counts = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getInvoice().getPaymentStatus(),
                        Collectors.counting()
                ));

        // Build DTO list
        List<PaymentDistributionSummaryDto> dtos = new ArrayList<>();
        for (Invoice.PaymentStatus status : Invoice.PaymentStatus.values()) {
            long count = counts.getOrDefault(status, 0L);
            double percentage = (count * 100.0) / total;

            PaymentDistributionSummaryDto dto =PaymentDistributionSummaryDto.builder().percentagePayment(percentage).paymentStatus(status).build();

            dtos.add(dto);
        }

        return dtos;
    }

    //transaction per monthly summary
    public List<PaymentMonthlyTransactionSummaryDto>  paymentMonthlyTransactionSummaryDto(int year,UUID apartmentId) {
        return paymentRepository.monthlyTransactionSummaryByYear(year,apartmentId).stream().map(item->
                PaymentMonthlyTransactionSummaryDto.builder()
                        .period(Month.of(item.getMonth()).name())
                        .count(item.getCount())
                        .build()).toList();
    }

    public List<PaymentYearlyTransactionSummaryDto> paymentYearlyTransactionSummaryDto(UUID apartmentId) {
        return paymentRepository.yearlyTransactionSummaryByYear(apartmentId).stream().map(item->
                        PaymentYearlyTransactionSummaryDto.builder()
                                .count(item.getCount())
                                .period(item.getYear())
                                .build())
                .toList();
    }



}
