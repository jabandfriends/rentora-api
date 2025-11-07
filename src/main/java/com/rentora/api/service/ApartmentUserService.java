package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.model.dto.ApartmentUser.Request.ApartmentUserCreateRequestDto;
import com.rentora.api.model.dto.ApartmentUser.Request.ApartmentUserUpdateRequestDto;
import com.rentora.api.model.dto.Tenant.Response.CreateApartmentUserResponseDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.User;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentUserService {
    private final ApartmentUserRepository apartmentUserRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;

    public CreateApartmentUserResponseDto addToApartment(ApartmentUserCreateRequestDto request,
                                                         UUID apartmentId, UUID userId, UUID adminId) {
        //apartment
        Apartment apartment = apartmentRepository.findById(apartmentId).orElse(null);
        //user
        User user = userRepository.findById(userId).orElse(null);
        //admin
        User userAdmin = userRepository.findById(adminId).orElse(null);

        ApartmentUser apartmentUser = new ApartmentUser();
        apartmentUser.setApartment(apartment);
        apartmentUser.setUser(user);
        apartmentUser.setCreatedBy(userAdmin);
        apartmentUser.setRole(request.getRole());

        apartmentUserRepository.save(apartmentUser);

        return toCreateApartmentUserResponseDto(apartmentUser);

    }

    public void updateApartmentUser(ApartmentUserUpdateRequestDto request){
        ApartmentUser aptUser = apartmentUserRepository.findById(request.getApartmentUserId())
                .orElseThrow(()-> new BadRequestException("User not found in this apartment."));

        if(request.getRole()!=null ) aptUser.setRole(request.getRole());
        if(request.getIsActive()!=null ) aptUser.setIsActive(request.getIsActive());
        apartmentUserRepository.save(aptUser);
    }
    public static CreateApartmentUserResponseDto toCreateApartmentUserResponseDto(ApartmentUser apartmentUser) {
        CreateApartmentUserResponseDto response = new CreateApartmentUserResponseDto();
        response.setApartmentUserId(apartmentUser.getId());
        return response;
    }
}
