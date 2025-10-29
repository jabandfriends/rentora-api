package com.rentora.api.repository;

import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.MaintenanceSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MaintenanceSupplyRepository extends JpaRepository<MaintenanceSupply, UUID>, JpaSpecificationExecutor<MaintenanceSupply> {
    List<MaintenanceSupply> findByMaintenance(Maintenance maintenance);
}
