package com.rentora.api.controller;


import com.rentora.api.model.dto.Apartment.Request.CreateApartmentRequest;
import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.Apartment.Response.ExecuteApartmentResponse;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Maintenance.Metadata.MaintenanceMetadataResponseDto;
import com.rentora.api.model.dto.Maintenance.Request.CreateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceDetailDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenanceInfoDTO;
import com.rentora.api.model.dto.Maintenance.Response.MaintenancePageResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.MaintenanceService;
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
@RequestMapping("/api/apartment/{apartmentId}/maintenance")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<MaintenanceInfoDTO>>> getMaintenance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Maintenance.Status status
    ) {

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        // Call the service method with the correct parameters
        Page<MaintenanceInfoDTO> response = maintenanceService.getMaintenance(
                apartmentId, search, status, pageable);
        MaintenanceMetadataResponseDto maintenanceInfoDto = maintenanceService.getMaintenanceMetadata(response.getContent());

        // The ApiResponse.success method should be adjusted to accept the correct DTO
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(response,page,maintenanceInfoDto)));
    }

    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<MaintenanceDetailDTO>> getMaintenanceById(
            @PathVariable UUID maintenanceId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MaintenanceDetailDTO maintenance = maintenanceService.getMaintenanceById(maintenanceId);
        return ResponseEntity.ok(ApiResponse.success(maintenance));
    }


    @PostMapping("/users/create")
    public ResponseEntity<ApiResponse<ExecuteMaintenanceResponse>> createMaintenance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateMaintenanceRequest request) {

        ExecuteMaintenanceResponse response = maintenanceService.createMaintenance(currentUser.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Maintenance request created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<ExecuteMaintenanceResponse>> updateMaintenance(
            @PathVariable UUID apartmentId,
            @PathVariable UUID maintenanceId,
            @RequestBody @Valid UpdateMaintenanceRequest request) {

        ExecuteMaintenanceResponse response = maintenanceService.updateMaintenance(maintenanceId, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance update successfully", response));
    }

    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(@PathVariable UUID apartmentId, @PathVariable UUID maintenanceId) {
        maintenanceService.deleteMaintenance(maintenanceId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance delete successfully", null));
    }
}
