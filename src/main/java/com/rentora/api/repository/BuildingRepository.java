package com.rentora.api.repository;

import com.rentora.api.model.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID>, JpaSpecificationExecutor<Building> {

    Page<Building> findByApartmentId(UUID apartmentId, Pageable pageable);
    List<Building> findByApartmentId(UUID apartmentId);

    @Query("SELECT b FROM Building b WHERE b.apartment.id = :apartmentId AND b.name LIKE %:name%")
    Page<Building> findByApartmentIdAndNameContaining(@Param("apartmentId") UUID apartmentId,
                                                      @Param("name") String name,
                                                      Pageable pageable);

    @Query("SELECT b FROM Building b " +
            "JOIN b.apartment.apartmentUsers au " +
            "WHERE b.id = :buildingId AND au.user.id = :userId AND au.isActive = true")
    Optional<Building> findByIdAndUserId(@Param("buildingId") UUID buildingId,
                                         @Param("userId") UUID userId);

    long countByApartmentId(UUID apartmentId);

    @Query("SELECT COUNT(b) FROM Building b WHERE b.apartment.id = :apartmentId")
    long countByApartment_Id(
            @Param("apartmentId") UUID apartmentId
    );

    boolean existsByApartmentIdAndName(UUID apartmentId, String name);

    Page<Building> findAll(Specification<Building> spec, java.awt.print.Pageable pageable);
}