package com.rentora.api.service;

import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.ApartmentUserRepository;
import com.rentora.api.repository.UserRepository;
import com.rentora.api.specifications.ApartmentUserSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantService {

    private final ApartmentUserRepository apartmentUserRepository;

    public Page<TenantInfoDto> getTenants(String name,UUID apartmentId,Pageable pageable) {
        Specification<ApartmentUser> spec = ApartmentUserSpecification.hasApartmentId(apartmentId).and(ApartmentUserSpecification.isActive()).and(ApartmentUserSpecification.hasName(name));
        Page<ApartmentUser>  apartmentUsers = apartmentUserRepository.findAll(spec, pageable);
        return apartmentUsers.map(TenantService::toTenantInfoDto);
    }

    public static TenantInfoDto toTenantInfoDto(ApartmentUser user){
        TenantInfoDto tenant = new TenantInfoDto();
        tenant.setFullName(user.getUser().getFullName());
        tenant.setEmail(user.getUser().getEmail());
        tenant.setPhoneNumber(user.getUser().getPhoneNumber());
        tenant.setUserId(user.getId());

        List<Contract> contracts = user.getUser().getContracts();

        // Check contracts
        boolean occupied = contracts.stream()
                .anyMatch(contract -> contract.getStatus() == Contract.ContractStatus.ACTIVE);
        tenant.setOccupiedStatus(occupied);

        //check roomnum with active
        contracts.stream()
                .filter(contract -> contract.getStatus() == Contract.ContractStatus.ACTIVE)
                .findFirst()
                .map(Contract::getUnit)                       // get the unit
                .map(Unit::getUnitName)                       // get the unit name
                .ifPresent(tenant::setUnitName);

        tenant.setCreatedAt(user.getCreatedAt());
        return tenant;
    }
}
