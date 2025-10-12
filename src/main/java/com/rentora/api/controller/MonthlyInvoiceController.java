package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.MonthlyInvoice.Metadata.MonthlyInvoiceMetadataDto;
import com.rentora.api.model.dto.MonthlyInvoice.Request.CreateMonthlyInvoiceDto;
import com.rentora.api.model.dto.MonthlyInvoice.Response.MonthlyInvoiceResponseDto;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.MonthlyInvoiceService;
import jakarta.validation.Valid;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/monthly/invoices")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyInvoiceController {
    private final MonthlyInvoiceService monthlyInvoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createMonthlyInvoice(@AuthenticationPrincipal UserPrincipal currentUser , @RequestBody @Valid CreateMonthlyInvoiceDto request){
        monthlyInvoiceService.createMonthlyInvoice(currentUser,request.getUnitId(),request.getReadingMonth(),request.getReadingYear(),request.getPaymentDueDay());

        return ResponseEntity.ok(ApiResponse.success("success",null));
    }

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<MonthlyInvoiceResponseDto>>> getAllMonthlyInvoice(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) Invoice.PaymentStatus paymentStatus
    ){
        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);
        Page<MonthlyInvoiceResponseDto> monthlyInvoices = monthlyInvoiceService.getAllMonthlyInvoice(paymentStatus,unitName,buildingName,apartmentId,pageable);

        MonthlyInvoiceMetadataDto monthlyInvoiceMetadata = monthlyInvoiceService.getMonthlyInvoiceMetadata(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(monthlyInvoices,page,monthlyInvoiceMetadata)));
    }
}
