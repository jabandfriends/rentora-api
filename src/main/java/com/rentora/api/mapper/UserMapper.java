package com.rentora.api.mapper;

import com.rentora.api.model.dto.ApartmentUser.Request.ApartmentUserCreateRequestDto;
import com.rentora.api.model.dto.ApartmentUser.Request.ApartmentUserUpdateRequestDto;
import com.rentora.api.model.dto.Authentication.CreateUserRequest;
import com.rentora.api.model.dto.Authentication.UpdateUserRequestDto;
import com.rentora.api.model.dto.Tenant.Response.CreateApartmentUserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public CreateUserRequest toCreateUserRequestFromApartmentUserRequest(ApartmentUserCreateRequestDto request) {
        CreateUserRequest user = new CreateUserRequest();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalId(request.getNationalId());
        user.setBirthDate(request.getBirthDate());
        user.setEmergencyContactName(request.getEmergencyContactName());
        user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        return user;
    }

    public UpdateUserRequestDto toUpdateUserRequestFromApartmentUserRequest(ApartmentUserUpdateRequestDto request) {
        UpdateUserRequestDto user = new UpdateUserRequestDto();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalId(request.getNationalId());
        user.setBirthDate(request.getBirthDate());
        user.setEmergencyContactName(request.getEmergencyContactName());
        user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        return user;
    }
}
