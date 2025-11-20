package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApartmentUserRepository extends JpaRepository<ApartmentUser, UUID>, JpaSpecificationExecutor<ApartmentUser> {
    Long countByApartmentId(UUID apartmentId);
    Long countByApartmentIdAndIsActiveTrue(UUID apartmentId);
    Long countByApartmentIdAndIsActiveFalse(UUID apartmentId);

    Optional<ApartmentUser> findByApartmentAndUser(Apartment apartment, User user);

    @Query("""
    SELECT au 
    FROM ApartmentUser au
    JOIN FETCH au.user u
    JOIN FETCH au.apartment a
    LEFT JOIN FETCH u.contracts c
    LEFT JOIN FETCH c.unit
    """)
    List<ApartmentUser> findAllWithRelations();

}

