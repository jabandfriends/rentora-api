package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Maintenance.Request.UpdateMaintenanceRequest;
import com.rentora.api.model.dto.Maintenance.Response.ExecuteMaintenanceResponse;
import com.rentora.api.repository.MaintenanceRepository;
import com.rentora.api.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    @PutMapping("{maintenanceId}")
    public ResponseEntity<ApiResponse<ExecuteMaintenanceResponse>> updateMaintenance(
            @PathVariable UUID maintenanceId,
            @RequestBody @Valid UpdateMaintenanceRequest request) {

        ExecuteMaintenanceResponse response = maintenanceService.updateMaintenance(maintenanceId, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance update successfully", response));
    }
}
