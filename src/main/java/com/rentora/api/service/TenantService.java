package com.rentora.api.service;

import com.rentora.api.constant.enums.UserRole;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ForbiddenRoleException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.mapper.TenantMapper;
import com.rentora.api.model.dto.Authentication.FirstTimePasswordResetRequestDto;
import com.rentora.api.model.dto.Authentication.UserInfo;
import com.rentora.api.model.dto.Tenant.Metadata.TenantsMetadataResponseDto;
import com.rentora.api.model.dto.Tenant.Response.TenantDetailInfoResponseDto;
import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.model.dto.Tenant.Response.TenantPageResponse;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.User;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.ApartmentUserSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantService {

    private final ApartmentUserRepository apartmentUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TenantMapper tenantMapper;

    public Page<TenantInfoDto> getTenants(String status,String name,UUID apartmentId,Pageable pageable) {

        Specification<ApartmentUser> spec = ApartmentUserSpecification.hasApartmentId(apartmentId).and(ApartmentUserSpecification.hasName(name));
        if(status != null) {
            log.info("status:{}",status);
            spec = spec.and(ApartmentUserSpecification.hasStatus(status.equals("active")));
        }
        Page<ApartmentUser>  apartmentUsers = apartmentUserRepository.findAll(spec, pageable);


        return apartmentUsers.map(tenantMapper::toTenantInfoDto);
    }

    public TenantsMetadataResponseDto getTenantsMetadata(UUID apartmentId) {

        Long totalTenant = apartmentUserRepository.countByApartmentId(apartmentId);
        Long totalOccupiedTenant= userRepository.countByApartmentIdAndIsActiveTrueWithContractStatus(apartmentId, Contract.ContractStatus.active);
        Long totalActiveTenant = apartmentUserRepository.countByApartmentIdAndIsActiveTrue(apartmentId);
        Long totalUnoccupiedTenant = userRepository.countUsersWithoutOrInactiveContract(apartmentId, Contract.ContractStatus.active);

        return TenantsMetadataResponseDto.builder().totalTenants(totalTenant).totalActiveTenants(totalActiveTenant)
                .totalOccupiedTenants(totalOccupiedTenant).totalUnoccupiedTenants(totalUnoccupiedTenant).build();
    }

    public TenantDetailInfoResponseDto getTenantDetail(UUID aptUserId) {
        ApartmentUser aptUser = apartmentUserRepository.findById(aptUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment user not found."));

        return tenantMapper.toTenantInfoDtoByUser(aptUser);
    }

    public void changePassword(UUID userId, FirstTimePasswordResetRequestDto request) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());

    }




}
