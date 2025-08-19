package com.rentora.api.controller;

import com.rentora.api.constant.ApiStatusMessage;
import com.rentora.api.dto.UnitDTO;
import com.rentora.api.entity.UnitEntity;
import com.rentora.api.model.ApiResponse;
import com.rentora.api.service.UnitsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/apartment/unit")
public class UnitsController {
    public final UnitsService unitsService;

    public UnitsController(UnitsService unitsService) {
        this.unitsService = unitsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitEntity>>> getUnitsByApartmentId(@RequestParam Long apartmentId) {
        List<UnitEntity> data = unitsService.getUnitsByApartment(apartmentId);
        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS,data));
    }

    @PostMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<List<UnitEntity>>> createUnits(
            @PathVariable Long apartmentId,
            @RequestBody List<UnitDTO> unitsDto) {
        List<UnitEntity> createdData = unitsService.createUnits(apartmentId, unitsDto);
        if(unitsDto.isEmpty()){
            throw new RuntimeException("No data provided");
        }
        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS, createdData));
    }

    @PutMapping()
    public ResponseEntity<ApiResponse<List<UnitEntity>>> updateUnits(
            @RequestBody List<UnitDTO> unitsDto) {
        List<UnitEntity> updated = unitsService.updateUnits(unitsDto);
        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS, updated));
    }

    @DeleteMapping()
    public ResponseEntity<ApiResponse<Void>> deleteUnits(
            @RequestBody List<Long> unitIds) {
        unitsService.deleteUnits(unitIds);
        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS, null));
    }
}
