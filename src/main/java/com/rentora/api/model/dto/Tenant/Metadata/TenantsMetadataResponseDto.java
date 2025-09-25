package com.rentora.api.model.dto.Tenant.Metadata;

import lombok.Data;

@Data
public class TenantsMetadataResponseDto {
    private Integer totalTenants;
    private long totalOccupiedTenant;
    private long totalUnOccupiedTenant;
}
