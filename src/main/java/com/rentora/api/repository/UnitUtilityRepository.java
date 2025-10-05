package com.rentora.api.repository;


import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;
import java.util.UUID;

public interface UnitUtilityRepository extends JpaRepository<UnitUtilities, UUID>, JpaSpecificationExecutor<UnitUtilities> {
    Page<UnitUtilities> findByUnitId(UUID unitId, Pageable pageable);
}
