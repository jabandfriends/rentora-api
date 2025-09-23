package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Floor.Request.CreateFloorRequestDto;
import com.rentora.api.model.dto.Floor.Response.CreateFloorResponseDto;
import com.rentora.api.service.FloorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/apartments/floor")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FloorController {

    final private FloorService floorService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateFloorResponseDto>> createFloor(@Valid @RequestBody CreateFloorRequestDto request){

        CreateFloorResponseDto response = floorService.createFloor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Create floor success", response));
    }
}
