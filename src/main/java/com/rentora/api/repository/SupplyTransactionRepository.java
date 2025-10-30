package com.rentora.api.repository;

import com.rentora.api.model.entity.SupplyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SupplyTransactionRepository extends JpaRepository<SupplyTransaction, UUID>, JpaSpecificationExecutor<SupplyTransaction> {
}
