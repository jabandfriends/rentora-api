package com.rentora.api.repository;

import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.UUID;

public interface UnitUtilityRepository extends JpaRepository<UnitUtilities, UUID> {

}
