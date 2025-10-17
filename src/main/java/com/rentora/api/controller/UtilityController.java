package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Utility.Request.UpdateUtilityDto;
import com.rentora.api.model.dto.Utility.Response.UtilitySummaryResponseDto;
import com.rentora.api.service.UtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/utility")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UtilityController {
    private final UtilityService utilityService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilitySummaryResponseDto>>> getUtilityByApartmentId(@PathVariable UUID apartmentId) {
        List<UtilitySummaryResponseDto> result = utilityService.getUtilityByApartmentId(apartmentId);

        return ResponseEntity.ok(ApiResponse.success("success",result));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Object>> updateUtilityByApartmentId(@PathVariable UUID apartmentId, @RequestBody UpdateUtilityDto request) {
        utilityService.updateUtilityByApartmentId(apartmentId, request);
        return ResponseEntity.ok(ApiResponse.success("success"));
    }

}
