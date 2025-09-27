package com.rentora.api.model.dto.Tenant.Response;

import lombok.Data;

import java.util.List;

@Data
public class TenantPageResponse {
    private List<TenantInfoDto> tenants;
    private long totalTenants;
    private long occupiedCount;
    private long unoccupiedCount;
    private int currentPage;
    private int totalPages;

    // getters & setters
}