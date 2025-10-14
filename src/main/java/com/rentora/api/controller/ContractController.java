package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Contract.Request.CreateContractRequest;
import com.rentora.api.model.dto.Contract.Request.TerminateContractRequest;
import com.rentora.api.model.dto.Contract.Request.UpdateContractRequest;
import com.rentora.api.model.dto.Contract.Response.ContractDetailDto;
import com.rentora.api.model.dto.Contract.Response.ContractSummaryDto;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/{apartmentId}/contracts")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ContractSummaryDto>>> getContracts(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Contract.ContractStatus contractStatus) {

        int requestPage = Math.max(page-1,0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestPage, size, sort);

        Page<ContractSummaryDto> contracts = contractService.getContractsByStatusByApartmentId(apartmentId,contractStatus, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(contracts,page)));
    }


    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractDetailDto>> getContractById(
            @PathVariable UUID apartmentId,
            @PathVariable UUID contractId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ContractDetailDto contract = contractService.getContractById(contractId);
        return ResponseEntity.ok(ApiResponse.success(contract));
    }



    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<ContractDetailDto>> getActiveContractByUnitId(
            @PathVariable UUID apartmentId,
            @PathVariable UUID unitId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ContractDetailDto contract = contractService.getContractByUnitId(unitId);
        return ResponseEntity.ok(ApiResponse.success(contract));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContractDetailDto>> createContract(
            @PathVariable UUID apartmentId,
            @Valid @RequestBody CreateContractRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ContractDetailDto contract = contractService.createContract(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contract created successfully", contract));
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractDetailDto>> updateContract(
            @PathVariable UUID apartmentId,
            @PathVariable UUID contractId,
            @Valid @RequestBody UpdateContractRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ContractDetailDto contract = contractService.updateContract(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("Contract updated successfully", contract));
    }

    @PostMapping("/{contractId}/terminate")
    public ResponseEntity<ApiResponse<ContractDetailDto>> terminateContract(
            @PathVariable UUID apartmentId,
            @PathVariable UUID contractId,
            @Valid @RequestBody TerminateContractRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ContractDetailDto contract = contractService.terminateContract(contractId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Contract terminated successfully", contract));
    }
}