package com.rentora.api.controller;

import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceCategorySummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceSummaryDTO;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceYearlySummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Maintenance.MaintenanceYearlyTableSummaryDto;
import com.rentora.api.model.dto.Analytic.Response.Payment.*;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.service.AnalyticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/analytic/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AnalyticController {
    private final AnalyticService analyticService;

    //------------------maintenance---------------------
    @GetMapping("/maintenance/category")
    public ResponseEntity<ApiResponse<List<MaintenanceCategorySummaryDto>>> maintenanceCategorySummary(@PathVariable UUID apartmentId) {
        List<MaintenanceCategorySummaryDto> maintenanceCategorySummary = analyticService.maintenanceCategorySummary(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(maintenanceCategorySummary));
    }

    @GetMapping("/maintenance/yearly/table")
    public ResponseEntity<ApiResponse<List<MaintenanceYearlyTableSummaryDto>>> maintenanceYearlyTableSummary(@PathVariable UUID apartmentId) {
        List<MaintenanceYearlyTableSummaryDto> result = analyticService.maintenanceYearlyTableSummary(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //get maintenance available year list
    @GetMapping("/maintenance/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getMaintenanceYears(@PathVariable UUID apartmentId){
        List<Integer> result = analyticService.getAvailableYearList(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    @GetMapping("/maintenance/monthly")
    public ResponseEntity<ApiResponse<List<MaintenanceSummaryDTO>>> maintenanceMonthlySummary(
            @PathVariable UUID apartmentId,
            @RequestParam Integer year){
        List<MaintenanceSummaryDTO> result = analyticService.getMonthlySummary(year,apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/maintenance/yearly")
    public ResponseEntity<ApiResponse<List<MaintenanceYearlySummaryDto>>> maintenanceYearly(@PathVariable UUID apartmentId){
        List<MaintenanceYearlySummaryDto> result = analyticService.getYearlySummary(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //------------------payment---------------------
    @GetMapping("/payment/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getPaymentYears(@PathVariable UUID apartmentId){
        List<Integer> result = analyticService.getAvailableYearsPayment(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/payment/monthly")
    public ResponseEntity<ApiResponse<List<PaymentMonthlySummaryDto>>> getPaymentMonthlySummary(
            @PathVariable UUID apartmentId,
            @RequestParam Integer year){
        List<PaymentMonthlySummaryDto> result = analyticService.getMonthlySummaryPayment(year,apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/payment/yearly")
    public ResponseEntity<ApiResponse<List<PaymentYearlySummaryDto>>> getPaymentYearly(@PathVariable UUID apartmentId){
        List<PaymentYearlySummaryDto> result = analyticService.getYearlySummaryPayment(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //stat
    @GetMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentStatsSummaryDto>> getPaymentStats(@PathVariable UUID apartmentId){
        PaymentStatsSummaryDto result = analyticService.paymentStatsSummary(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //transaction
    @GetMapping("/payment/monthly/transaction")
    public ResponseEntity<ApiResponse<List<PaymentMonthlyTransactionSummaryDto>>> getPaymentMonthlyTransaction(@PathVariable UUID apartmentId,
                                                                                                               @RequestParam Integer year){
        List<PaymentMonthlyTransactionSummaryDto> result = analyticService.paymentMonthlyTransactionSummaryDto(year,apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/payment/yearly/transaction")
    public ResponseEntity<ApiResponse<List<PaymentYearlyTransactionSummaryDto>>> getPaymentYearlyTransaction(@PathVariable UUID apartmentId){
        List<PaymentYearlyTransactionSummaryDto> result = analyticService.paymentYearlyTransactionSummaryDto(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //distribution
    @GetMapping("/payment/distribution")
    public ResponseEntity<ApiResponse<List<PaymentDistributionSummaryDto>>> getPaymentDistributionSummary(@PathVariable UUID apartmentId){
        List<PaymentDistributionSummaryDto> result = analyticService.paymentDistributionSummaryDto(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
