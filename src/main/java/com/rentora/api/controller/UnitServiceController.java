package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.UnitService.Response.UnitServiceInfoDTO;
import com.rentora.api.repository.UnitServiceRepository;
import com.rentora.api.service.UnitServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/all-room/detail/{unitId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitServiceController {

    private final UnitServiceService unitServiceService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitServiceInfoDTO>>> getUnitServicesByUnit(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId) {

        List<UnitServiceInfoDTO> serviceList  = unitServiceService.getUnitServicesByUnit(unitId);

        return ResponseEntity.ok(ApiResponse.success(serviceList));
    }
}
