package com.rentora.api.controller;


import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.dto.Supply.Request.CreateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Request.UpdateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Response.SupplyMetaDataDto;
import com.rentora.api.model.dto.Supply.Response.SupplySummaryResponseDto;
import com.rentora.api.model.entity.Supply;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.SupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/supply")
@RequiredArgsConstructor()
public class SupplyController {
    private final SupplyService supplyService;

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<SupplySummaryResponseDto>>> getAllSupplies(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search, //supplyName
            @RequestParam(required = false) Supply.SupplyCategory category
            ){

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        //data
        Page<SupplySummaryResponseDto> supply = supplyService.getAllSupplies(apartmentId,search,category,pageable);

        //metadata
        SupplyMetaDataDto supplyMetadata = supplyService.getSupplyMetadata(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(supply,page,supplyMetadata)));
    }

    @PostMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<Objects>> createSupply(@PathVariable UUID apartmentId, @RequestBody @Valid CreateSupplyRequestDto request){
        supplyService.createSupply(apartmentId,request);

        return ResponseEntity.ok(ApiResponse.success(null));

    }

    @PutMapping("/edit/{supplyId}")
    public ResponseEntity<ApiResponse<Objects>> updateSupply(@PathVariable UUID supplyId,@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody @Valid UpdateSupplyRequestDto request){
        supplyService.updateSupplies(supplyId,currentUser.getId(),request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/delete/{supplyId}")
    public ResponseEntity<ApiResponse<Objects>> deleteSupply(@PathVariable UUID supplyId){
        supplyService.deleteSupplies(supplyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
