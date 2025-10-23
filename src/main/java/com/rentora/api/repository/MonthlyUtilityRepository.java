package com.rentora.api.repository;

import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MonthlyUtilityRepository extends JpaRepository<UnitUtilities, UUID>, JpaSpecificationExecutor<UnitUtilities> {

    List<UnitUtilities> findAllByUnitId(UUID unitId);
}
