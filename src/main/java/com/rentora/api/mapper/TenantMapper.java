package com.rentora.api.mapper;

import com.rentora.api.model.dto.Tenant.Response.TenantDetailInfoResponseDto;
import com.rentora.api.model.dto.Tenant.Response.TenantInfoDto;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.User;
import com.rentora.api.model.entity.elastic.ApartmentUserDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class TenantMapper {
    public TenantDetailInfoResponseDto toTenantInfoDtoByUser(ApartmentUser aptUser){
        TenantDetailInfoResponseDto tenant = new TenantDetailInfoResponseDto();
        User user = aptUser.getUser();
        tenant.setUserId(user.getId());
        tenant.setApartmentUserId(aptUser.getId());
        tenant.setFirstName(user.getFirstName());
        tenant.setLastName(user.getLastName());
        tenant.setFullName(user.getFullName());
        tenant.setEmail(user.getEmail());
        tenant.setPhoneNumber(user.getPhoneNumber());
        tenant.setNationalId(user.getNationalId());
        tenant.setDateOfBirth(user.getBirthDate());
        tenant.setEmergencyContactName(user.getEmergencyContactName());
        tenant.setEmergencyContactPhone(user.getEmergencyContactPhone());
        tenant.setCreatedAt(user.getCreatedAt());
        tenant.setRole(aptUser.getRole());
        tenant.setIsActive(aptUser.getIsActive());

        return tenant;
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
        tenant.setIsActive(user.getIsActive());



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

    public ApartmentUserDocument toApartmentUserDocument(ApartmentUser user){
        ApartmentUserDocument tenant = new ApartmentUserDocument();
        tenant.setFullName(user.getUser().getFullName());
        tenant.setEmail(user.getUser().getEmail());
        tenant.setPhoneNumber(user.getUser().getPhoneNumber());
        tenant.setUserId(user.getUser().getId());
        tenant.setApartmentUserId(user.getId());
        tenant.setRole(user.getRole().toString());
        tenant.setAccountStatus(user.getIsActive());
        tenant.setIsActive(user.getIsActive());
        tenant.setApartmentId(user.getApartment().getId());



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

        Long createdAt = user.getCreatedAt() != null ? user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        tenant.setCreatedAt(
                createdAt
        );
        return tenant;
    }
}
