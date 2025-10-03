package com.rentora.api.controller;

import java.util.UUID;

import com.rentora.api.model.dto.Invoice.Metadata.InvoiceOverallDTO;
import com.rentora.api.model.dto.Invoice.Response.OverdueInvoiceOverallDTO;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.Invoice.Response.InvoiceSummaryDTO;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.dto.Invoice.Response.InvoiceDetailDTO;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.InvoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InvoiceController {
    
    private final InvoiceService invoiceService;

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<InvoiceSummaryDTO>>> getInvoices(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @PathVariable UUID apartmentId,
        @RequestParam(required = false) Invoice.PaymentStatus status){


        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<InvoiceSummaryDTO> summary = invoiceService.search(search, status, pageable, apartmentId);

        InvoiceOverallDTO overall = invoiceService.getInvoiceOverall(summary.getContent());

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(summary, page, overall)));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<PaginatedResponse<InvoiceSummaryDTO>>> getOverdueInvoices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "createdAt") String sortBy){

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<InvoiceSummaryDTO> overdue = invoiceService.searchOverdue(search, pageable);

        OverdueInvoiceOverallDTO overall = invoiceService.getOverdueInvoiceOverall(overdue.getContent());

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(overdue, page, overall)));
    }

     @GetMapping("/detail/{invoiceId}")
     public ResponseEntity<ApiResponse<InvoiceDetailDTO>> getInvoicesById(
         @PathVariable UUID invoiceId,
         @AuthenticationPrincipal UserPrincipal currentUser) {

         InvoiceDetailDTO invoice = invoiceService.getInvoicesById(invoiceId, currentUser.getId());
         return ResponseEntity.ok(ApiResponse.success(invoice));
     }

}
