package com.rentora.api.controller;


import com.rentora.api.model.dto.ApartmentService.Request.ApartmentServiceCreateRequestDto;
import com.rentora.api.model.dto.ApartmentService.Request.ApartmentServiceUpdateRequestDto;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Building.Response.BuildingSummaryDto;
import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.repository.UnitRepository;
import com.rentora.api.repository.UnitServiceRepository;
import com.rentora.api.service.ApartmentServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/apartment-services")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentServiceController {

    private final ApartmentServiceService apartmentServiceService;

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<List<ServiceInfoDTO>>> getAllServiceDetails
            (@PathVariable UUID apartmentId,@RequestParam(required = false) String activeService) {
        Boolean isActive = activeService == null ? null : Boolean.valueOf(activeService);
        List<ServiceInfoDTO> services  = apartmentServiceService.getApartmentService(apartmentId,isActive);

        return ResponseEntity.ok(ApiResponse.success("success",services));
    }

    @PostMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<Objects>> createApartmentService(@PathVariable UUID apartmentId,
                                                                       @Valid @RequestBody ApartmentServiceCreateRequestDto request){
        apartmentServiceService.createApartmentService(apartmentId,request);
        return ResponseEntity.ok(ApiResponse.success("success",null));
    }

    @PutMapping()
    public ResponseEntity<ApiResponse<Objects>> updateApartmentService(@Valid @RequestBody ApartmentServiceUpdateRequestDto request){
        apartmentServiceService.updateApartmentService(request);
        return ResponseEntity.ok(ApiResponse.success("success",null));
    }

}
