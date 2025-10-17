package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Request.UpdateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.model.dto.Floor.Response.FloorResponseRequestDto;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.service.FloorService;
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
@RequestMapping("/api/apartments/floor")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FloorController {

    final private FloorService floorService;

    @GetMapping("/{buildingId}")
    public ResponseEntity<ApiResponse<List<FloorResponseRequestDto>>> getFloors(
            @PathVariable UUID buildingId
    ) {
        List<FloorResponseRequestDto> result = floorService.getFloorByBuilding(buildingId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/detail/{floorId}")
    public ResponseEntity<ApiResponse<FloorResponseRequestDto>> getFloorDetails(
           @PathVariable UUID floorId
    ){
        FloorResponseRequestDto floor = floorService.getFloorById(floorId);
        return ResponseEntity.ok(ApiResponse.success(floor));
    }

    @DeleteMapping("/{floorId}")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @PathVariable UUID floorId
    ){
        floorService.deleteFloorById(floorId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{floorId}")
    public ResponseEntity<ApiResponse<Void>> updateFloor(@PathVariable UUID floorId,
    @RequestBody UpdateFloorRequestDto request){
        floorService.updateFloorById(floorId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateFloorResponseDto>> createFloor(@Valid @RequestBody CreateFloorRequestDto request){

        CreateFloorResponseDto response = floorService.createFloor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Create floor success", response));
    }
}
