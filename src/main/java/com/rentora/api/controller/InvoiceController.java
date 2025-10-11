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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InvoiceController {
    
    private final AdhocInvoiceService adhocInvoiceService;
//
//    @GetMapping("/{apartmentId}")
//    public ResponseEntity<ApiResponse<PaginatedResponse<InvoiceSummaryDTO>>> getInvoices(
//        @RequestParam(defaultValue = "1") int page,
//        @RequestParam(defaultValue = "10") int size,
//        @RequestParam(required = false) String search,
//        @RequestParam(defaultValue = "desc") String sortDir,
//        @RequestParam(defaultValue = "createdAt") String sortBy,
//        @PathVariable UUID apartmentId,
//        @RequestParam(required = false) Invoice.PaymentStatus status){
//
//
//        int requestedPage = Math.max(page - 1, 0);
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() :
//                Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(requestedPage, size, sort);
//
//        Page<InvoiceSummaryDTO> summary = invoiceService.search(search, status, pageable, apartmentId);
//
//        InvoiceOverallDTO overall = invoiceService.getInvoiceOverall(summary.getContent());
//
//        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(summary, page, overall)));
//    }

//    @GetMapping("/{apartmentId}/overdue")
//    public ResponseEntity<ApiResponse<PaginatedResponse<InvoiceSummaryDTO>>> getOverdueInvoices(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String search,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @PathVariable UUID apartmentId,
//            @RequestParam(defaultValue = "createdAt") String sortBy){
//
//        int requestedPage = Math.max(page - 1, 0);
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() :
//                Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(requestedPage, size, sort);
//
//        Page<InvoiceSummaryDTO> overdue = invoiceService.searchOverdue(search, pageable, apartmentId);
//
//        OverdueInvoiceOverallDTO overall = invoiceService.getOverdueInvoiceOverall(overdue.getContent());
//
//        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(overdue, page, overall)));
//    }

//     @GetMapping("/{apartmentId}/detail/{invoiceId}")
//     public ResponseEntity<ApiResponse<InvoiceDetailDTO>> getInvoicesById(
//         @PathVariable UUID invoiceId,
//         @PathVariable UUID apartmentId,
//         @AuthenticationPrincipal UserPrincipal currentUser) {
//
//         InvoiceDetailDTO invoice = invoiceService.getInvoicesById(invoiceId, currentUser.getId(),  apartmentId);
//         return ResponseEntity.ok(ApiResponse.success(invoice));
//     }

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdhocInvoiceSummaryDTO>>> getInvoices(
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

    @GetMapping("/{apartmentId}/overdue")
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

    @GetMapping("/{apartmentId}/detail/{adhocInvoiceId}")
    public ResponseEntity<ApiResponse<AdhocInvoiceDetailDTO>> getInvoicesById(
            @PathVariable UUID adhocInvoiceId,
            @PathVariable UUID apartmentId) {

        AdhocInvoiceDetailDTO invoice = adhocInvoiceService.getAdhocInvoicesById(adhocInvoiceId,  apartmentId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @PostMapping("/users/create")
    public ResponseEntity<ApiResponse<ExecuteAdhocInvoiceResponse>> createInvoice(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateAdhocInvoiceRequest request){

        ExecuteAdhocInvoiceResponse response = adhocInvoiceService.createAdhocInvoice(currentUser.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Adhoc invoice created successfully",response), HttpStatus.CREATED);
    }

}
