package com.rentora.api.repository;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByIdAndIsActiveTrue(UUID id);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.apartmentUsers au WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailWithApartments(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.apartmentUsers au WHERE u.id = :id AND u.isActive = true")
    Optional<User> findByIdWithApartments(@Param("id") UUID id);

    @Query("""
    SELECT COUNT(DISTINCT u)
    FROM User u
    JOIN u.apartmentUsers au
    JOIN u.contracts c
    WHERE au.apartment.id = :apartmentId
    AND u.isActive = true
      AND au.isActive = true
      AND c.status = :contractStatus
""")
    Long countByApartmentIdAndIsActiveTrueWithContractStatus(UUID apartmentId, Contract.ContractStatus contractStatus);


    @Query("""
    SELECT COUNT(DISTINCT u)
    FROM User u
    JOIN u.apartmentUsers au
    LEFT JOIN u.contracts c
    WHERE au.apartment.id = :apartmentId
      AND u.isActive = true
      AND au.isActive = true
      AND (c IS NULL OR c.status <> :activeStatus)
""")
    Long countUsersWithoutOrInactiveContract(
            UUID apartmentId,
            Contract.ContractStatus activeStatus
    );


}