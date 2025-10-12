package com.rentora.api.model.dto.Tenant.Metadata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantsMetadataResponseDto {
    private Long totalTenants;
    private Long totalOccupiedTenants;
    private Long totalUnoccupiedTenants;
    private Long totalActiveTenants;
}
