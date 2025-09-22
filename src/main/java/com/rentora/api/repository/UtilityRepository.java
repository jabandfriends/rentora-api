package com.rentora.api.repository;

import com.rentora.api.model.entity.Utility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UtilityRepository extends JpaRepository<Utility, UUID> {
}
