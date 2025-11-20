package com.rentora.api.repository;

import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.Unit;
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
public interface UnitRepository extends JpaRepository<Unit, UUID>, JpaSpecificationExecutor<Unit> {

    @Query("SELECT u FROM Unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId")
    Page<Unit> findByApartmentId(@Param("apartmentId") UUID apartmentId, Pageable pageable);

    Page<Unit> findByFloorId(UUID floorId, Pageable pageable);
    @Query("SELECT u FROM Unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:unitType IS NULL OR u.unitType = :unitType)")
    Page<Unit> findByApartmentIdAndFilters(@Param("apartmentId") UUID apartmentId,
                                           @Param("status") Unit.UnitStatus status,
                                           @Param("unitType") Unit.UnitType unitType,
                                           Pageable pageable);

    @Query("SELECT u FROM Unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "JOIN b.apartment.apartmentUsers au " +
            "WHERE u.id = :unitId AND au.user.id = :userId AND au.isActive = true")
    Optional<Unit> findByIdAndUserId(@Param("unitId") UUID unitId,
                                     @Param("userId") UUID userId);

    @Query("SELECT COUNT(u) FROM Unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId")
    long countByApartmentId(@Param("apartmentId") UUID apartmentId);


    @Query("SELECT COUNT(u) FROM Unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId AND u.status = :status")
    long countByApartmentIdAndStatus(@Param("apartmentId") UUID apartmentId,
                                     @Param("status") Unit.UnitStatus status);
    @Query("SELECT COUNT(u) FROM Unit u " +
            "JOIN u.floor f " +
            "WHERE f.building.id = :buildingId")
    long countByBuildingId(@Param("buildingId") UUID buildingId);

    @Query("SELECT COUNT(u) FROM Unit u " +
            "JOIN u.floor f " +
            "WHERE f.building.id = :buildingId AND u.status = :status")
    long countByBuildingIdAndStatus(@Param("buildingId") UUID buildingId,
                                    @Param("status") Unit.UnitStatus status);

    boolean existsByFloorIdAndUnitName(UUID floorId, String unitName);

    long countByFloor(Floor floor);

    long countByFloorAndStatus(Floor floor, Unit.UnitStatus status);

}