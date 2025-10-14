package com.rentora.api.repository;


import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitUtilityRepository extends JpaRepository<UnitUtilities, UUID>, JpaSpecificationExecutor<UnitUtilities> {
    Page<UnitUtilities> findByUnitId(UUID unitId, Pageable pageable);
    @Query("SELECT SUM(u.usageAmount) " +
            "FROM UnitUtilities u " +
            "JOIN u.utility utility " +
            "JOIN u.unit unit " +
            "JOIN unit.floor floor " +
            "JOIN floor.building building " +
            "JOIN building.apartment apartment " +
            "WHERE apartment.id = :apartmentId " +
            "AND utility.utilityName = :utilityName")
    long countUsageAmountByApartmentIdByUtility(
            @Param("apartmentId") UUID apartmentId,
            @Param("utilityName") String utilityName
    );

    @Query("SELECT SUM(CASE " +
            "           WHEN u.utility.utilityType = 'fixed' THEN u.utility.fixedPrice " +
            "           WHEN u.utility.utilityType = 'meter' THEN u.usageAmount * u.utility.unitPrice " +
            "           ELSE 0 END) " +
            "FROM UnitUtilities u " +
            "JOIN u.unit unit " +
            "JOIN unit.floor floor " +
            "JOIN floor.building building " +
            "JOIN building.apartment apartment " +
            "WHERE apartment.id = :apartmentId " +
            "AND u.utility.utilityName = :utilityName")
    BigDecimal sumPriceByUtility(
            @Param("apartmentId") UUID apartmentId,
            @Param("utilityName") String utilityName
    );

    @Query("""
    SELECT DISTINCT u.usageMonth
    FROM UnitUtilities u
    JOIN u.unit unit
    JOIN unit.floor f
    JOIN f.building b
    JOIN b.apartment a
    WHERE a.id = :apartmentId
""")
    List<LocalDate> findAllUsageMonthsByApartment(@Param("apartmentId") UUID apartmentId);

    @Query("""
    SELECT DISTINCT uu.usageMonth
    FROM UnitUtilities uu
    JOIN uu.unit u
    JOIN u.floor f
    JOIN f.building b
    WHERE b.apartment.id = :apartmentId
      AND b.name = :buildingName
""")
    List<LocalDate> findAllUsageMonthsByApartmentAndBuilding(
            @Param("apartmentId") UUID apartmentId,
            @Param("buildingName") String buildingName
    );

    List<UnitUtilities> findByUnitAndUsageMonth(Unit unit, LocalDate usageMonth);

}
