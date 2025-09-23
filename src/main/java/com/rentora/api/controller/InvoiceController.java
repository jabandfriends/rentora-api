package com.rentora.api.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
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

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<InvoiceSummaryDTO>>> getInvoices(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
//      @RequestParam(defaultValue = "status") String sortBy,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Invoice.PaymentStatus status){


        int requestedPage = Math.max(page - 1, 0);
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
////                Sort.by(sortBy).descending() :
////                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size);

        Page<InvoiceSummaryDTO> invoices = invoiceService.search(search, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(invoices,page)));
    }

    // @GetMapping("/{invoiceId}")
    // public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoicesById(
    //     @PathVariable UUID invoiceId,
    //     @AuthenticationPrincipal UserPrincipal currentUser) {

    //     InvoiceDTO invoiceDTO = invoiceService.getInvoicesById(invoiceId, currentUser.getId());
    //     return ResponseEntity.ok(ApiResponse.success(invoiceDTO));
    // }

    // @DeleteMapping("/{invoiceId}")
    // public ResponseEntity<ApiResponse<Void>> deleteInvoice(
    //     @PathVariable UUID invoiceId,
    //     @AuthenticationPrincipal UserPrincipal currentUser) {
    //     invoiceService.deleteInvoice(invoiceId, currentUser.getId());
    //     return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully", null));
    // }
}
