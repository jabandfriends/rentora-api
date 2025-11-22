package com.rentora.api.model.dto.ApartmentUser.Request;

import com.rentora.api.constant.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InviteUserRequestDto {
    private String email;
    private UserRole role;
}
