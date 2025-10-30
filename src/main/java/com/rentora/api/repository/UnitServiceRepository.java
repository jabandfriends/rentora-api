package com.rentora.api.repository;

import com.rentora.api.model.entity.UnitServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UnitServiceRepository extends JpaRepository<UnitServiceEntity, UUID> {

    List<UnitServiceEntity> findAllByUnitId(UUID UnitId);

    Optional<UnitServiceEntity> findByUnitIdAndServiceEntityId(UUID unitId, UUID serviceId);
}

