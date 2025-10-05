package com.rentora.api.repository;


import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;
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

}
