package com.rentora.api.controller;

import com.rentora.api.entity.ApartmentsEntity;
import com.rentora.api.model.ApiResponse;
import com.rentora.api.model.PaginatedResponse;
import com.rentora.api.model.Pagination;
import com.rentora.api.service.ApartmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import com.rentora.api.constant.ApiStatusMessage;


@RestController
@RequestMapping("/api/apartments")
public class ApartmentsController {

    private final ApartmentsService apartmentsService;

    @Autowired
    public ApartmentsController(ApartmentsService apartmentsService) {
        this.apartmentsService = apartmentsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ApartmentsEntity>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name) {

        Page<ApartmentsEntity> result = apartmentsService.getAll(name,PageRequest.of(page-1, size));

        Pagination pagination = new Pagination(
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements()
        );

        PaginatedResponse<ApartmentsEntity> responseData = new PaginatedResponse<>(result.getContent(), pagination);

        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS, responseData));
    }


    @GetMapping("/id")
    public ResponseEntity<ApiResponse<ApartmentsEntity>> getById(@RequestParam Long id) {
        ApartmentsEntity result = apartmentsService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, ApiStatusMessage.SUCCESS, result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApartmentsEntity>> create(@RequestBody  ApartmentsEntity apartmentsEntity) {
        apartmentsService.create(apartmentsEntity);
        return ResponseEntity.ok(new ApiResponse<>(true,ApiStatusMessage.SUCCESS,null));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ApartmentsEntity>> update(@RequestBody ApartmentsEntity apartmentsEntity) {
        apartmentsService.update(apartmentsEntity);
        return ResponseEntity.ok(new ApiResponse<>(true,ApiStatusMessage.SUCCESS,null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ApartmentsEntity>> delete(@RequestParam Long id) {
        apartmentsService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true,ApiStatusMessage.SUCCESS,null));
    }
}
