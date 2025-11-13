package com.rentora.api.model.dto.Apartment.Request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateApartmentPaymentResponseDto {
    private UUID apartmentPaymentId;
    private String presignedUrl;
}
