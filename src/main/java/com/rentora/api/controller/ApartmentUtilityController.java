package com.rentora.api.controller;

import com.rentora.api.model.dto.ApartmentUtility.response.YearlyUtilityDetailDTO;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.ApartmentUtility.response.ApartmentUtilityUsageSummaryDTO;
import com.rentora.api.service.ApartmentUtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;
import java.time.Year;

@Slf4j
@RestController
@RequestMapping("/api/apartment/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentUtilityController {

    private final ApartmentUtilityService apartmentUtilityService;

    @GetMapping("/apartmentUtility")
    public ResponseEntity<ApiResponse<ApartmentUtilityUsageSummaryDTO>> getUtilitySummary(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int year) {

        ApartmentUtilityUsageSummaryDTO summary = apartmentUtilityService
                .getUtilitySummary(apartmentId, year);

        if (summary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Apartment utility data not found.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Annual utility usage summary for year %d retrieved successfully.", year),
                summary
        ));
    }

    @GetMapping("/YearlyApartmentUtility")
    public ResponseEntity<ApiResponse<List<YearlyUtilityDetailDTO>>> getYearlyUtilityDetail(
            @PathVariable UUID apartmentId) {

        List<YearlyUtilityDetailDTO> yearlyDetails = apartmentUtilityService.getYearlyUtilityDetail(apartmentId);

        if (yearlyDetails == null || yearlyDetails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No yearly utility data found for this apartment.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Yearly utility usage summary retrieved successfully.",
                yearlyDetails
        ));
    }

}