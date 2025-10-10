package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Building.Request.CreateBuildingRequest;
import com.rentora.api.model.dto.Building.Request.UpdateBuildingRequest;
import com.rentora.api.model.dto.Building.Response.BuildingDetailDto;
import com.rentora.api.model.dto.Building.Response.BuildingSummaryDto;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.BuildingService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/buildings")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BuildingController {

    private final BuildingService buildingService;
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BuildingSummaryDto>>> getBuildings(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        int requestPage = Math.max(page-1,0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();


        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<BuildingSummaryDto> buildings = buildingService.getBuildingsByApartment(apartmentId, search, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(buildings,page)));
    }

    @GetMapping("/no/paginate")
    public ResponseEntity<ApiResponse<List<BuildingSummaryDto>>> getBuildingsNoPaginate(
            @PathVariable UUID apartmentId) {


        List<BuildingSummaryDto> buildings = buildingService.getBuildingsByApartmentNoPaginate(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(buildings));
    }

    @GetMapping("/{buildingId}")
    public ResponseEntity<ApiResponse<BuildingDetailDto>> getBuildingById(
            @PathVariable UUID apartmentId,
            @PathVariable UUID buildingId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        BuildingDetailDto building = buildingService.getBuildingById(buildingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(building));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BuildingDetailDto>> createBuilding(
            @PathVariable UUID apartmentId,
            @Valid @RequestBody CreateBuildingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        request.setApartmentId(apartmentId); // Ensure apartment ID matches path
        BuildingDetailDto building = buildingService.createBuilding(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Building created successfully", building));
    }

    @PutMapping("/{buildingId}")
    public ResponseEntity<ApiResponse<BuildingDetailDto>> updateBuilding(
            @PathVariable UUID apartmentId,
            @PathVariable UUID buildingId,
            @Valid @RequestBody UpdateBuildingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        BuildingDetailDto building = buildingService.updateBuilding(buildingId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Building updated successfully", building));
    }

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(
            @PathVariable UUID apartmentId,
            @PathVariable UUID buildingId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        buildingService.deleteBuilding(buildingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Building deleted successfully", null));
    }
}
