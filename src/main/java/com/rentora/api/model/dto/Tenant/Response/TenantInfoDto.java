package com.rentora.api.model.dto.Tenant.Response;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TenantInfoDto {

    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;

    private boolean occupiedStatus;

    private String unitName;

    public static class ContractDto {
        private UUID contractId;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
    }

}