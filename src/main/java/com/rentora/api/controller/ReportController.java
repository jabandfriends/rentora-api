package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.Report.Response.ReceiptReportDetailDTO;
import com.rentora.api.model.dto.Unit.Response.UnitSummaryDto;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.ReceiptReportRepository;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.ReceiptReportService;
import com.rentora.api.service.ReportService;
import com.rentora.api.service.UnitService;
import com.rentora.api.utility.EnumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/report")
@RequiredArgsConstructor
public class ReportController {

    private final UnitService unitService;
    private final ReportService reportService;
    private final ReceiptReportService receiptReportService;

    @GetMapping("/{apartmentId}/unit")
    public ResponseEntity<ApiResponse<PaginatedResponse<UnitSummaryDto>>> getUnits(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "unitName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Unit.UnitStatus status,
            @RequestParam(required = false) String unitType,
            @RequestParam(required = false) UUID floorId) {

        int requestPage = Math.max(page-1,0);
        Unit.UnitType type = null;
        try {
            type = EnumUtils.parseUnitType(unitType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<UnitSummaryDto> units = unitService.getUnitsByApartment(
                apartmentId, status, type, floorId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(units,page)));
    }

    @GetMapping("/{apartmentId}/utility")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReportService.UnitServiceResponseDto>>> getUnits(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int requestPage = Math.max(page-1, 0);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<ReportService.UnitServiceResponseDto> units = reportService.getUnitsUtility(apartmentId,pageable);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(units,page)));
    }
    @GetMapping("/{apartmentId}/adhoc-invoices")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReceiptReportDetailDTO>>> getAdhocInvoices(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) AdhocInvoice.PaymentStatus status
    ) {
        int requestPage = Math.max(page - 1, 0);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<ReceiptReportDetailDTO> invoices = receiptReportService.getAdhocInvoices(apartmentId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(invoices, page)));
    }
}
