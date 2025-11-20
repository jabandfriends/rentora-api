package com.rentora.api.model.dto.Contract.Response;

import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
@Builder
public class ContractUpdateResponseDto {
    URL presignedUrl;
    UUID contractId;
}
