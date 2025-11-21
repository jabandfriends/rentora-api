package com.rentora.api.controller;

import com.rentora.api.model.dto.Apartment.Metadata.ApartmentMetadataDto;
import com.rentora.api.model.dto.Apartment.Metadata.UpdateApartmentPaymentRequestDto;
import com.rentora.api.model.dto.Apartment.Request.CreateApartmentRequest;
import com.rentora.api.model.dto.Apartment.Request.SetupApartmentRequest;
import com.rentora.api.model.dto.Apartment.Request.UpdateApartmentPaymentResponseDto;
import com.rentora.api.model.dto.Apartment.Request.UpdateApartmentRequest;
import com.rentora.api.model.dto.Apartment.Response.ApartmentDetailDTO;
import com.rentora.api.model.dto.Apartment.Response.ApartmentPaymentSummaryResponseDto;
import com.rentora.api.model.dto.Apartment.Response.ApartmentSummaryDTO;
import com.rentora.api.model.dto.Apartment.Response.ExecuteApartmentResponse;
import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.PaginatedResponse;
import com.rentora.api.model.dto.PaginatedResponseWithMetadata;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.ApartmentService;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentController {


    private final ApartmentService apartmentService;
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ApartmentSummaryDTO>>> getApartments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Apartment.ApartmentStatus status
    ) {

        int requestedPage = Math.max(page - 1, 0);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(requestedPage, size, sort);

        Page<ApartmentSummaryDTO> apartments = apartmentService.getApartments(
                currentUser.getId(), search,status, pageable);
        ApartmentMetadataDto apartmentMetadataDto = apartmentService.getApartmentsMetadata(apartments.getContent());

        return ResponseEntity.ok(ApiResponse.success(PaginatedResponseWithMetadata.of(apartments,page,apartmentMetadataDto)));
    }

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<ApartmentDetailDTO>> getApartmentById(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ApartmentDetailDTO apartment = apartmentService.getApartmentById(apartmentId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(apartment));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExecuteApartmentResponse>> createApartment(
            @Valid @RequestBody CreateApartmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ExecuteApartmentResponse apartment = apartmentService.createApartment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Apartment created successfully", apartment));
    }

    @PutMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<ExecuteApartmentResponse>> updateApartment(
            @PathVariable UUID apartmentId,
            @Valid @RequestBody UpdateApartmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ExecuteApartmentResponse apartment = apartmentService.updateApartment(apartmentId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Apartment updated successfully", apartment));
    }

    @DeleteMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteApartment(
            @PathVariable UUID apartmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        apartmentService.deleteApartment(apartmentId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Apartment deleted successfully", null));
    }

    @PostMapping("/setup/{apartmentId}")
    public ResponseEntity<ApiResponse<Void>> setupApartment(@PathVariable UUID apartmentId , @Valid @RequestBody SetupApartmentRequest request, @AuthenticationPrincipal UserPrincipal currentUser) {
        apartmentService.apartmentSetup(apartmentId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Apartment setup successfully", null));
    }

    //get apartment  payment
    @GetMapping("/{apartmentId}/payment")
    public ResponseEntity<ApiResponse<ApartmentPaymentSummaryResponseDto>> getApartmentPayments(
            @PathVariable UUID apartmentId
    ){
       ApartmentPaymentSummaryResponseDto apartmentPayments = apartmentService.getApartmentPayments(apartmentId);
       return ResponseEntity.ok(ApiResponse.success(apartmentPayments));
    }

    //get apartment payment by id
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<ApartmentPaymentSummaryResponseDto>> getApartmentPaymentById(
            @PathVariable UUID paymentId
    ){
        ApartmentPaymentSummaryResponseDto apartmentPayment = apartmentService.getApartmentPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(apartmentPayment));
    }

    @GetMapping("/{apartmentId}/payment/active")
    public ResponseEntity<ApiResponse<ApartmentPaymentSummaryResponseDto>> getActiveApartmentPaymentById(
            @PathVariable UUID apartmentId
    ){
        ApartmentPaymentSummaryResponseDto apartmentPayment = apartmentService.getActiveApartmentPaymentById(apartmentId);
        return ResponseEntity.ok(ApiResponse.success(apartmentPayment));
    }

    @PutMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<UpdateApartmentPaymentResponseDto>> updateApartmentPayment(@PathVariable UUID paymentId,
                                                                                                 @Valid @RequestBody UpdateApartmentPaymentRequestDto request){
        UpdateApartmentPaymentResponseDto response = apartmentService.updateApartmentPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("success", response));
    }

    @DeleteMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deleteApartmentPayment(@PathVariable UUID paymentId){
        apartmentService.deleteApartmentPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("success", null));
    }
}
