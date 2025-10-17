package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Payment.Response.PaymentMonthlyAvenue;
import com.rentora.api.repository.PaymentRepository;
import com.rentora.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<PaymentMonthlyAvenue>> getMonthlyRevenue(@PathVariable UUID apartmentId) {
        PaymentMonthlyAvenue summary = paymentService.getMonthlyData(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
