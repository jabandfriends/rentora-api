package com.rentora.api.repository;

import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApartmentUserRepository extends JpaRepository<ApartmentUser, UUID>, JpaSpecificationExecutor<ApartmentUser> {
    Long countByApartmentId(UUID apartmentId);
    Long countByApartmentIdAndIsActiveTrue(UUID apartmentId);
    Long countByApartmentIdAndIsActiveFalse(UUID apartmentId);


}

