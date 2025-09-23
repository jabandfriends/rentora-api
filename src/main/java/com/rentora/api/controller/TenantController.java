package com.rentora.api.controller;

import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.Authentication.CreateUserRequest;
import com.rentora.api.model.dto.Authentication.UserInfo;
import com.rentora.api.model.dto.Contract.Response.ContractSummaryDto;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.Tenant.Response.CreateApartmentUserResponseDto;
import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.ApartmentUserService;
import com.rentora.api.service.AuthService;
import com.rentora.api.service.TenantService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments/manage/tenant")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TenantController {

    private final TenantService tenantService;
    private final AuthService authService;
    private final ApartmentUserService  apartmentUserService;
    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<TenantInfoDto>>> getTenants(@PathVariable UUID apartmentId,    @RequestParam(defaultValue = "1") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,@RequestParam(required = false) String name,@RequestParam(defaultValue = "createdAt") String sortBy,@RequestParam(defaultValue = "asc") String sortDir){
        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(requestedPage, size,sort);

        Page<TenantInfoDto> apartments = tenantService.getTenants(name,apartmentId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(apartments,page)));

    }

    @PostMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<CreateApartmentUserResponseDto>> createTenants(@PathVariable UUID apartmentId ,@AuthenticationPrincipal UserPrincipal currentUser, @Valid @RequestBody CreateUserRequest request) {

        UserInfo newUser = authService.createUser(request);
        UUID newUserId = UUID.fromString(newUser.getId());
        CreateApartmentUserResponseDto apartmentUser = apartmentUserService.addToApartment(apartmentId,newUserId,currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("Create Tenant Successfully",apartmentUser));
    }

}
