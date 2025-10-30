package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.SupplyTransaction;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.SupplyTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/apartments/supply/transactions/{apartmentId}")
@RequiredArgsConstructor()
public class SupplyTransactionsController {
    private final SupplyTransactionService supplyTransactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SupplyTransactionSummaryResponseDto>>> getSupplyTransactions(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String supplyName,
            @RequestParam(required = false) SupplyTransaction.SupplyTransactionType transactionType
            ){
        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<SupplyTransactionSummaryResponseDto> transactions = supplyTransactionService.getSupplyTransactions(apartmentId
        ,supplyName,transactionType,pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(transactions,page)));

    }




}
