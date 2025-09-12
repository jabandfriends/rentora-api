package com.rentora.api.repository;

import com.rentora.api.entity.Floor;
import com.rentora.api.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FloorRepository extends JpaRepository<Floor, UUID> {

    long countByBuildingId(UUID buildingId);

    @Query("SELECT COUNT(u) FROM Unit u JOIN u.floor f WHERE f.building.id = :buildingId")
    long countUnitsByBuildingId(@Param("buildingId") UUID buildingId);

    @Query("SELECT COUNT(u) FROM Unit u JOIN u.floor f WHERE f.building.id = :buildingId AND u.status = :status")
    long countUnitsByBuildingIdAndStatus(@Param("buildingId") UUID buildingId,
                                         @Param("status") Unit.UnitStatus status);

    boolean existsByBuildingIdAndFloorNumber(UUID buildingId, Integer floorNumber);
}
