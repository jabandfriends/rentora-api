package com.rentora.api.repository;


import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface UnitUtilityRepository extends JpaRepository<UnitUtilities, UUID> {
    Page<UnitUtilities> findByUnitId(UUID unitId, Pageable pageable);
}
