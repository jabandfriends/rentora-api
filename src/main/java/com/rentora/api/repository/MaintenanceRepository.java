package com.rentora.api.repository;

import com.rentora.api.model.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID>, JpaSpecificationExecutor<Maintenance> {

}