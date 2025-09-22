package com.rentora.api.repository;

import com.rentora.api.model.entity.Maintenance;
import com.sun.tools.javac.Main;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
}