package com.rentora.api.model.dto.Invoice.Request;

import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
@Builder
public class AdhocUpdateRequestResponseDto {
    private UUID invoiceId;
    private URL presignedUrl;
}
