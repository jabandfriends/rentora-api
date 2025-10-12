package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.UnitService.Request.CreateUnitServiceRequest;
import com.rentora.api.model.dto.UnitService.Response.ExecuteUnitServiceResponse;
import com.rentora.api.model.dto.UnitService.Response.UnitServiceInfoDTO;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitServiceRepository;
import com.rentora.api.service.UnitServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/all-room/detail/{unitId}")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitServiceController {

    private final UnitServiceService unitServiceService;
    private final UnitRepository unitRepository;


    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitServiceInfoDTO>>> getUnitServicesByUnit(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId) {

        List<UnitServiceInfoDTO> serviceList  = unitServiceService.getUnitServicesByUnit(unitId);

        return ResponseEntity.ok(ApiResponse.success(serviceList));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ExecuteUnitServiceResponse>> createUnitService(
            @PathVariable UUID unitId,
            @Valid @RequestBody CreateUnitServiceRequest request){


        ExecuteUnitServiceResponse response = unitServiceService.createUnitService(unitId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ExecuteUnitServiceResponse>> deleteUnitService(
            @PathVariable UUID unitId,
            @RequestParam UUID unitServiceId
            ) {
        unitServiceService.deleteUnitService(unitServiceId);

        return ResponseEntity.ok(ApiResponse.success("Unit Service delete successfully", null));

    }
}
