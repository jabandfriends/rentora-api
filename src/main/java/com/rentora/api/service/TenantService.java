package com.rentora.api.service;

import com.rentora.api.constant.enums.UserRole;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.exception.ForbiddenRoleException;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.dto.Authentication.FirstTimePasswordResetRequestDto;
import com.rentora.api.model.dto.Tenant.Metadata.TenantsMetadataResponseDto;
import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.model.dto.Tenant.Response.TenantPageResponse;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.User;
import com.rentora.api.repository.ApartmentUserRepository;
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

    public Page<TenantInfoDto> getTenants(String status,String name,UUID apartmentId,Pageable pageable) {

        Specification<ApartmentUser> spec = ApartmentUserSpecification.hasApartmentId(apartmentId).and(ApartmentUserSpecification.hasName(name));
        if(status != null) {
            log.info("status:{}",status);
            spec = spec.and(ApartmentUserSpecification.hasStatus(status.equals("active")));
        }
        Page<ApartmentUser>  apartmentUsers = apartmentUserRepository.findAll(spec, pageable);


        return apartmentUsers.map(this::toTenantInfoDto);
    }

    public TenantsMetadataResponseDto getTenantsMetadata(List<TenantInfoDto> tenantInfoDto) {
        TenantsMetadataResponseDto tenantsMetadataResponseDto = new TenantsMetadataResponseDto();
        tenantsMetadataResponseDto.setTotalTenants(tenantInfoDto.size());
        tenantsMetadataResponseDto.setTotalOccupiedTenant(tenantInfoDto.stream().filter(TenantInfoDto::isOccupiedStatus).count());
        tenantsMetadataResponseDto.setTotalUnOccupiedTenant(tenantInfoDto.stream().filter(tenant->!tenant.isOccupiedStatus()).count());
        return tenantsMetadataResponseDto;
    }

    public void changePassword(UUID userId, FirstTimePasswordResetRequestDto request) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());

    }

    public TenantInfoDto toTenantInfoDto(ApartmentUser user){
        TenantInfoDto tenant = new TenantInfoDto();
        tenant.setFullName(user.getUser().getFullName());
        tenant.setEmail(user.getUser().getEmail());
        tenant.setPhoneNumber(user.getUser().getPhoneNumber());
        tenant.setUserId(user.getUser().getId());
        tenant.setApartmentUserId(user.getId());
        tenant.setRole(user.getRole());
        tenant.setAccountStatus(user.getIsActive());



        List<Contract> contracts = user.getUser().getContracts();

        // Check contracts
        boolean occupied = contracts.stream()
                .anyMatch(contract -> contract.getStatus() == Contract.ContractStatus.active);
        tenant.setOccupiedStatus(occupied);

        //check roomnum with active
        contracts.stream()
                .filter(contract -> contract.getStatus() == Contract.ContractStatus.active)
                .findFirst()
                .map(Contract::getUnit)                       // get the unit
                .map(Unit::getUnitName)                       // get the unit name
                .ifPresent(tenant::setUnitName);

        tenant.setCreatedAt(user.getCreatedAt());
        return tenant;
    }
}
