package com.rentora.api.model.dto.Authentication;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfo {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private boolean mustChangePassword;
    private LocalDate birthDate;
    private String nationalId;
    private String lastLogin;
    private List<ApartmentRole> apartmentRoles;

    @Data
    public static class ApartmentRole {
        private String apartmentId;
        private String apartmentName;
        private String role;
    }
}
