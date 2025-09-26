package com.rentora.api.model.dto.Tenant.Response;

import com.rentora.api.constant.enums.UserRole;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TenantDetailInfoResponseDto {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;


}
