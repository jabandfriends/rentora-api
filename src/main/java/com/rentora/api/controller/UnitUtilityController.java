package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.UnitUtility.Request.CreateUnitUtilityRequestDto;
import com.rentora.api.model.dto.UnitUtility.Request.UnitUtility;
import com.rentora.api.service.UnitUtilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
