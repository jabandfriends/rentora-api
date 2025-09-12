package com.rentora.api.controller;

import com.rentora.api.dto.ApiResponse;
import com.rentora.api.dto.PaginatedResponse;
import com.rentora.api.dto.Unit.Request.CreateUnitRequest;
import com.rentora.api.dto.Unit.Request.UpdateUnitRequest;
import com.rentora.api.dto.Unit.Response.UnitDetailDto;
import com.rentora.api.dto.Unit.Response.UnitSummaryDto;
import com.rentora.api.entity.Unit;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.UnitService;
import com.rentora.api.utility.EnumUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/units")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitController {


    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UnitSummaryDto>>> getUnits(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "unitName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Unit.UnitStatus status,
            @RequestParam(required = false) String unitType,
            @RequestParam(required = false) UUID floorId) {

        Unit.UnitType type = null;
        try {
            type = EnumUtils.parseUnitType(unitType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UnitSummaryDto> units = unitService.getUnitsByApartment(
                apartmentId, status, type, floorId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(units)));
    }

    @GetMapping("/{unitId}")
    public ResponseEntity<ApiResponse<UnitDetailDto>> getUnitById(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UnitDetailDto unit = unitService.getUnitById(unitId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(unit));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UnitDetailDto>> createUnit(
            @PathVariable UUID apartmentId,
            @Valid @RequestBody CreateUnitRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UnitDetailDto unit = unitService.createUnit(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Unit created successfully", unit));
    }

    @PutMapping("/{unitId}")
    public ResponseEntity<ApiResponse<UnitDetailDto>> updateUnit(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId,
            @Valid @RequestBody UpdateUnitRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UnitDetailDto unit = unitService.updateUnit(unitId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Unit updated successfully", unit));
    }

    @DeleteMapping("/{unitId}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        unitService.deleteUnit(unitId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Unit deleted successfully", null));
    }
}