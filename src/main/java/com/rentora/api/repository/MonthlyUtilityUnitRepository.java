package com.rentora.api.repository;

import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MonthlyUtilityUnitRepository extends
        JpaRepository<UnitUtilities, UUID>,
        JpaSpecificationExecutor<UnitUtilities> { // <--- ถูกต้องตามหลักการ

    List<UnitUtilities> findAllByUnitId(UUID unitId);


}