package com.rentora.api.repository;

import com.rentora.api.model.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID>, JpaSpecificationExecutor<Maintenance> {
    @Query("SELECT COUNT(m) FROM Maintenance m " +
            "JOIN m.unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE m.status = :status AND b.apartment.id = :apartmentId")
    long countMaintenanceByStatusAndApartmentId(@Param("status") Maintenance.Status status,
                                                @Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(m) FROM Maintenance m " + "JOIN m.unit u " + "JOIN u.floor f "+ "JOIN f.building b " +
    "WHERE b.apartment.id = :apartmentId")
    long countMaintenanceByApartmentId(@Param("apartmentId") UUID apartmentId);
}