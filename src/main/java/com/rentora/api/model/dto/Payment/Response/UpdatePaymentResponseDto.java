package com.rentora.api.model.dto.Payment.Request;

import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
@Builder
public class UpdatePaymentResponseDto {
    private UUID paymentId;
    private URL presignedURL;
}
