package com.rentora.api.model.dto.Apartment.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ExecuteApartmentResponse {
    private UUID apartmentId;
    private String presignedUrl; // for frontend to upload logo
    private String logoUrl;      // S3 key stored in DB
}