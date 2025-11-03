package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.MonthlyUtilityBuilding.Response.MonthlyUtilityBuildingDetailDTO;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.service.MonthlyUtilityBuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/apartment/{apartmentId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityBuildingController {

    private final MonthlyUtilityBuildingService monthlyUtilityBuildingService;
    private final ApartmentRepository apartmentRepository;

    @GetMapping("/buildingUtility")
    public ResponseEntity<ApiResponse<Map<UUID, MonthlyUtilityBuildingDetailDTO>>> getBuildingUtilitiesSummary(
            @PathVariable UUID apartmentId) {

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Apartment not found with ID: " + apartmentId));

        return monthlyUtilityBuildingService.getApartmentUtilitySummaryByBuilding(apartment);
    }
}