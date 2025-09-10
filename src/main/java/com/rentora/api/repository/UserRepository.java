package com.rentora.api.repository;

import com.rentora.api.entity.User;
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
}