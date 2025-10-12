package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.ExtraService.Response.ServiceInfoDTO;
import com.rentora.api.service.ApartmentServiceService;
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
@RequestMapping("/api/apartments/{apartmentId}/all-room/detail/test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentServiceController {

    private final ApartmentServiceService apartmentServiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceInfoDTO>>> getAllServiceDetails
            (@PathVariable UUID apartmentId) {
        List<ServiceInfoDTO> services  = apartmentServiceService.getApartmentService(apartmentId);

        return ResponseEntity.ok(ApiResponse.success(services));
    }
}
