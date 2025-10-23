package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.MonthlyUnitlity.Response.MonthlyUtilityUnitDetailDTO;

import com.rentora.api.service.MonthlyUtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartment/{apartmentId}/monthly-utility")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyUtilityController {
    private final MonthlyUtilityService monthlyUtilityService;

    @GetMapping("/{unitId}")
    public ResponseEntity<ApiResponse<MonthlyUtilityUnitDetailDTO>> getUnitUtilitiesSummary(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId) {

        MonthlyUtilityUnitDetailDTO summary = monthlyUtilityService.getMonthlyUtilitySummary(unitId);

        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved unit utility summary", summary));
    }
}
