package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Metadata.MonthlyUtilityBuildingMetadata;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingDetailDTO;
import com.rentora.api.model.dto.MonthlyUtilityFloor.Metadata.MonthlyUtilityFloorMetadata;
import com.rentora.api.model.dto.MonthlyUtilityFloor.Respose.MonthlyUtilityFloorDetailDto;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.BuildingRepository;
import com.rentora.api.service.MonthlyUtilityBuildingService;
import com.rentora.api.service.MonthlyUtilityFloorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartment/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityFloorController {

    private final MonthlyUtilityFloorService monthlyUtilityFloorService;
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;

    @GetMapping("/FloorUtility")
    public ResponseEntity<ApiResponse<PaginatedResponse<MonthlyUtilityFloorDetailDto>>> getFloorUtilitiesSummary(
            @PathVariable UUID apartmentId,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int size,
            @RequestParam(defaultValue = "floorName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Apartment not found with ID: " + apartmentId));

        Building building = null;
        if (buildingId != null) {
            building = buildingRepository.findById(buildingId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Building not found with ID: " + buildingId));
        }

        Page<MonthlyUtilityFloorDetailDto> summaryPage =
                monthlyUtilityFloorService.getApartmentUtilitySummaryByFloor(apartment,building, search, pageable);

        MonthlyUtilityFloorMetadata floorUtilityMetadata =
                monthlyUtilityFloorService.getMonthlyUtilityFloorMetadata(apartment, summaryPage.getContent());


        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(summaryPage, page, floorUtilityMetadata))
        );
    }
}