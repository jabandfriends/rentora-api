package com.rentora.api.controller;

import java.util.UUID;

import com.rentora.api.model.dto.Invoice.Metadata.AdhocInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Metadata.OverdueInvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Request.CreateAdhocInvoiceRequest;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceDetailDTO;
import com.rentora.api.model.dto.Invoice.Response.AdhocInvoiceSummaryDTO;
import com.rentora.api.model.dto.Invoice.Response.ExecuteAdhocInvoiceResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.service.AdhocInvoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/invoices/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InvoiceController {
    
    private final AdhocInvoiceService adhocInvoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AdhocInvoiceSummaryDTO>>> getAdhocInvoices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @PathVariable UUID apartmentId,
            @RequestParam(required = false) AdhocInvoice.PaymentStatus status){


        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<AdhocInvoiceSummaryDTO> summary = adhocInvoiceService.searchAdhocInvoiceByInvoiceNumber(search, status, pageable, apartmentId);

        AdhocInvoiceOverallDTO overall = adhocInvoiceService.getAdhocInvoiceOverall(summary.getContent());

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(summary, page, overall)));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdhocInvoiceSummaryDTO>>> getOverdueInvoices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "desc") String sortDir,
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "createdAt") String sortBy){

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<AdhocInvoiceSummaryDTO> overdue = adhocInvoiceService.searchAdhocInvoiceOverdue(search, pageable, apartmentId);

        OverdueInvoiceOverallDTO overall = adhocInvoiceService.getOverdueAdhocInvoiceOverall(overdue.getContent());

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(overdue, page, overall)));
    }

    @GetMapping("/detail/{adhocInvoiceId}")
    public ResponseEntity<ApiResponse<AdhocInvoiceDetailDTO>> getInvoicesById(
            @PathVariable UUID adhocInvoiceId,
            @PathVariable UUID apartmentId) {

        AdhocInvoiceDetailDTO invoice = adhocInvoiceService.getAdhocInvoicesById(adhocInvoiceId,  apartmentId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/tenant/{tenantUserId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdhocInvoiceSummaryDTO>>> getTenantAdhocInvoices(
            @PathVariable UUID apartmentId,
            @PathVariable UUID tenantUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) AdhocInvoice.PaymentStatus status,
            @RequestParam(required = false) AdhocInvoice.AdhocInvoiceCategory category) {

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<AdhocInvoiceSummaryDTO> summary = adhocInvoiceService.getAdhocInvoicesByTenant(tenantUserId, apartmentId, status, category, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(summary, page)));
    }

    @PostMapping("/adhocInvoice/create")
    public ResponseEntity<ApiResponse<ExecuteAdhocInvoiceResponse>> createAdhocInvoice(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID apartmentId,
            @Valid @RequestBody CreateAdhocInvoiceRequest request){

        ExecuteAdhocInvoiceResponse response = adhocInvoiceService.createAdhocInvoice(currentUser.getId(), apartmentId, request);
        return new ResponseEntity<>(ApiResponse.success("Adhoc invoice created successfully",response), HttpStatus.CREATED);
    }

}
