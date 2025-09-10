package com.rentora.api.repository;

import com.rentora.api.entity.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UnitsRepository extends JpaRepository<UnitEntity,Long> {
    List<UnitEntity> findByApartmentId(Long apartmentId);
}
