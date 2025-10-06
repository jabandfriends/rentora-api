package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.dto.Report.Metadata.ReportUnitUtilityMetadata;
import com.rentora.api.model.dto.Report.Response.ReadingDateDto;
import com.rentora.api.model.dto.Pagination;
import com.rentora.api.model.dto.Report.Response.ReceiptReportDetailDTO;
import com.rentora.api.model.dto.Unit.Response.UnitSummaryDto;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.ReceiptReportService;
import com.rentora.api.service.ReportService;
import com.rentora.api.service.UnitService;
import com.rentora.api.specifications.UnitSpecification;
import com.rentora.api.utility.EnumUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import com.rentora.api.repository.UnitRepository;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/report")
@RequiredArgsConstructor
public class ReportController {

    private final UnitService unitService;
    private final ReportService reportService;
    private final ReceiptReportService receiptReportService;
    private final UnitRepository unitRepository;

    @Transactional
    @GetMapping("/{apartmentId}/room-report")
    public ResponseEntity<ApiResponse<PaginatedResponse<UnitSummaryDto>>> getUnits(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "unitName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Unit.UnitStatus status,
            @RequestParam(required = false) String unitType,
            @RequestParam(required = false) UUID floorId,
            @RequestParam(required = false) String search
    ) {
        int requestPage = Math.max(page - 1, 0);
        Unit.UnitType type = null;

        try {
            type = EnumUtils.parseUnitType(unitType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<UnitSummaryDto> units = unitRepository.findAll((root, query, cb) -> {
            assert query != null;
            query.distinct(true);
            return UnitSpecification.hasApartmentId(apartmentId)
                    .and(UnitSpecification.hasStatus(status))
                    .and(UnitSpecification.hasUnitType(null))
                    .and(UnitSpecification.hasFloorId(floorId))
                    .and(UnitSpecification.hasName(search))
                    .toPredicate(root, query, cb);
        }, pageable).map(unitService::toUnitSummaryDto);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(units, page)));
    }

    @GetMapping("/{apartmentId}/utility")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReportService.UnitServiceResponseDto>>> getUnits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String unitName,
            @PathVariable UUID apartmentId,
            @RequestParam String readingDate
    ) {
        int requestPage = Math.max(page-1, 0);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<ReportService.UnitServiceResponseDto> units = reportService.getUnitsUtility(apartmentId,unitName,readingDate,pageable);
        ReportUnitUtilityMetadata metadata = reportService.getUnitsUtilityMetadata(apartmentId);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(units,page,metadata)));
    }

    @GetMapping("/{apartmentId}/reading/date/utility")
    public ResponseEntity<ApiResponse<List<ReadingDateDto>>> getUnitsReadingDate(
            @PathVariable UUID apartmentId
    ) {

        List<ReadingDateDto> unitsDate = reportService.getUnitUtilityReadingDate(apartmentId);


        return ResponseEntity.ok(ApiResponse.success(unitsDate));
    }




    @GetMapping("/{apartmentId}/receipt-report")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReceiptReportDetailDTO>>> getAdhocInvoices(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) AdhocInvoice.PaymentStatus status
    ) {
        int requestPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(requestPage, size, sort);

        List<ReceiptReportDetailDTO> invoices = receiptReportService.getAdhocAndInvoices(apartmentId, status, pageable);
        int totalItems = invoices.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        Pagination pagination = new Pagination(page, size, totalPages, totalItems);
        PaginatedResponse<ReceiptReportDetailDTO> response = new PaginatedResponse<>(invoices, pagination);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
