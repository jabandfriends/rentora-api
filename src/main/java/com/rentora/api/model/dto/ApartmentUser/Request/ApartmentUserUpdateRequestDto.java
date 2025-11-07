package com.rentora.api.model.dto.ApartmentUser.Request;

import com.rentora.api.constant.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ApartmentUserUpdateRequestDto {
    private UUID apartmentUserId;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    @Size(max = 13, message = "National ID must not exceed 13 characters")
    private String nationalId;

    private String birthDate; // Will be parsed to LocalDate

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Size(max = 15, message = "Emergency contact phone must not exceed 15 characters")
    private String emergencyContactPhone;

    private UserRole role;
    private Boolean isActive;
}
