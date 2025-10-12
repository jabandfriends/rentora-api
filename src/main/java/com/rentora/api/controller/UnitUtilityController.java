package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.UnitUtility.Request.CreateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Request.UnitUtility;
import com.rentora.api.model.dto.UnitUtility.Request.UpdateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Response.AvailableMonthsDto;
import com.rentora.api.model.dto.UnitUtility.Response.AvailableYearsDto;
import com.rentora.api.model.dto.UnitUtility.Response.UnitWithUtilityResponseDto;
import com.rentora.api.service.UnitUtilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/unit/utility")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitUtilityController {
    private final UnitUtilityService unitUtilityService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createUnitUtility(@PathVariable UUID apartmentId, @RequestBody CreateUnitUtilityRequestDto createUnitUtilityRequestDto) {
        unitUtilityService.createUnitUtility(apartmentId,createUnitUtilityRequestDto);

        return ResponseEntity.ok(ApiResponse.success("Create utility successfully",null));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Object>> updateUnitUtility(@PathVariable UUID apartmentId,@RequestBody UpdateUnitUtilityRequestDto request) {
        unitUtilityService.updateUnitUtility(apartmentId,request);

        return ResponseEntity.ok(ApiResponse.success("Update utility successfully",null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitWithUtilityResponseDto>>>
    unitWithUtility(@PathVariable UUID apartmentId, @RequestParam String buildingName) {
       List<UnitWithUtilityResponseDto> response = unitUtilityService.getAllUnitWithUtility(apartmentId,buildingName);
       return ResponseEntity.ok(ApiResponse.success("Success",response));
    }

    @GetMapping("/years")
    public ResponseEntity<ApiResponse<AvailableYearsDto>> getAvailableYears(@PathVariable UUID apartmentId) {
        AvailableYearsDto availableYearsDtoList = unitUtilityService.getAvailableYears(apartmentId);

        return ResponseEntity.ok(ApiResponse.success("Success",availableYearsDtoList));
    }

    @GetMapping("/months")
    public ResponseEntity<ApiResponse<AvailableMonthsDto>> getAvailableMonths(@PathVariable UUID apartmentId,
                                                 @RequestParam String buildingName,
                                                 @RequestParam int year) {
        AvailableMonthsDto availableMonthsDto = unitUtilityService.getAvailableMonths(apartmentId,buildingName, year);

        return ResponseEntity.ok(ApiResponse.success("Success",availableMonthsDto));
    }
}
