package com.rentora.api.repository;

import com.rentora.api.entity.Apartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    @Query("SELECT a FROM Apartment a " +
            "JOIN a.apartmentUsers au " +
            "WHERE au.user.id = :userId AND au.isActive = true")
    Page<Apartment> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT a FROM Apartment a " +
            "JOIN FETCH a.apartmentUsers au " +
            "WHERE a.id = :apartmentId AND au.user.id = :userId AND au.isActive = true")
    Optional<Apartment> findByIdAndUserId(@Param("apartmentId") UUID apartmentId,
                                          @Param("userId") UUID userId);

    @Query("SELECT a FROM Apartment a WHERE a.name LIKE %:name%")
    Page<Apartment> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT a FROM Apartment a " +
            "JOIN a.apartmentUsers au " +
            "WHERE au.user.id = :userId AND au.isActive = true " +
            "AND a.name LIKE %:name%")
    Page<Apartment> findByUserIdAndNameContaining(@Param("userId") UUID userId,
                                                  @Param("name") String name,
                                                  Pageable pageable);
}