package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {

    @Query("SELECT c FROM Contract c " +
            "JOIN c.unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId")
    Page<Contract> findByApartmentId(@Param("apartmentId") UUID apartmentId, Pageable pageable);

    Page<Contract> findByTenantId(UUID tenantId, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.unit.id = :unitId AND c.status = 'active'")
    Optional<Contract> findActiveContractByUnitId(@Param("unitId") UUID unitId);

    Optional<Contract> findContractByUnit(Unit unit);

    @Query("SELECT COUNT(c) FROM Contract c " +
            "JOIN c.unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId AND c.status = 'active'")
    long countActiveByApartmentId(@Param("apartmentId") UUID apartmentId);

    @Modifying
    @Query("UPDATE Contract c SET c.status = 'expired', c.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE c.endDate < CURRENT_DATE " +
            "AND c.status NOT IN ('terminated', 'renewed', 'expired')")
    void expireOldContracts();

    Optional<Contract> findByContractNumber(String contractNumber);
}