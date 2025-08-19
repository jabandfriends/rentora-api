package com.rentora.api.controller;

import com.rentora.api.constant.ApiStatusMessage;
import com.rentora.api.entity.ApartmentsEntity;
import com.rentora.api.entity.UnitEntity;
import com.rentora.api.model.ApiResponse;
import com.rentora.api.service.UnitsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/units")
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
}
