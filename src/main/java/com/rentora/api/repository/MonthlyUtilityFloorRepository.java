package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Floor;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MonthlyUtilityFloorRepository extends JpaRepository<UnitUtilities, UUID>, JpaSpecificationExecutor<UnitUtilities> {

    @Query("SELECT uu FROM UnitUtilities uu JOIN FETCH uu.unit u WHERE u.floor = :floor")
    List<UnitUtilities> findAllByUnit_Floor(@Param("floor") Floor floor);
}
