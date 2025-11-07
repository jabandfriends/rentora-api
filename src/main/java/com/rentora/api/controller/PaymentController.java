package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.dto.Payment.Request.UpdatePaymentRequestDto;
import com.rentora.api.model.dto.Payment.Response.PaymentMetadata;
import com.rentora.api.model.dto.Payment.Response.PaymentMonthlyAvenue;
import com.rentora.api.model.dto.Payment.Response.PaymentResponseDto;
import com.rentora.api.model.dto.Payment.Response.UpdatePaymentResponseDto;
import com.rentora.api.model.entity.Payment;
import com.rentora.api.repository.PaymentRepository;
import com.rentora.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/{apartmentId}/monthly")
    public ResponseEntity<ApiResponse<PaymentMonthlyAvenue>> getMonthlyRevenue(@PathVariable UUID apartmentId) {
        PaymentMonthlyAvenue summary = paymentService.getMonthlyData(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentResponseDto>>> getAllPayments(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(required = false) String buildingName,
            @RequestParam LocalDate genMonth
            ) {
        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);
        Page<PaymentResponseDto> payments = paymentService.getAllPayments(genMonth,apartmentId,buildingName,status,pageable);
        PaymentMetadata metadata = paymentService.getPaymentMetadata(apartmentId);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(payments,page,metadata)));
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<UpdatePaymentResponseDto>> updatePayment(@PathVariable UUID paymentId,
                                                                               @RequestBody UpdatePaymentRequestDto updatePaymentRequestDto) {
        UpdatePaymentResponseDto updatePaymentResponseDto = paymentService.updatePayment(paymentId, updatePaymentRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatePaymentResponseDto));
    }
}
