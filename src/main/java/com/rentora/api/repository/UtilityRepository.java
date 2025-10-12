package com.rentora.api.repository;

import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface UtilityRepository extends JpaRepository<Utility, UUID>, JpaSpecificationExecutor<Utility> {
    List<Utility> findByApartmentId(UUID apartmentId);
}
