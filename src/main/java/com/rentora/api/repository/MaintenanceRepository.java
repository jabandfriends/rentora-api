package com.rentora.api.repository;

import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.projection.maintenance.MaintenanceCategorySummaryProjection;
import com.rentora.api.model.projection.maintenance.MaintenanceMonthlySummary;
import com.rentora.api.model.projection.maintenance.MaintenanceYearlySummary;
import com.rentora.api.model.projection.maintenance.MaintenanceYearlySummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID>, JpaSpecificationExecutor<Maintenance> {
    @Query("SELECT COUNT(m) FROM Maintenance m " +
            "JOIN m.unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE m.status = :status AND b.apartment.id = :apartmentId")
    long countMaintenanceByStatusAndApartmentId(@Param("status") Maintenance.Status status,
                                                @Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(m) FROM Maintenance m " +
            "JOIN m.unit u " +
            "JOIN u.floor f " +
            "JOIN f.building b " +
            "WHERE m.category = :category AND b.apartment.id = :apartmentId")
    long countByCategoryAndApartmentId(@Param("category") Maintenance.Category category,@Param("apartmentId") UUID apartmentId);


    @Query("SELECT m.category AS category, COUNT(m) AS count " +
            "FROM Maintenance m " +
            "WHERE m.unit.floor.building.apartment.id = :apartmentId " +
            "GROUP BY m.category")
    List<MaintenanceCategorySummaryProjection> countMaintenanceByCategory(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(m) FROM Maintenance m " + "JOIN m.unit u " + "JOIN u.floor f "+ "JOIN f.building b " +
    "WHERE b.apartment.id = :apartmentId")
    long countMaintenanceByApartmentId(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(m) FROM Maintenance m " + "JOIN m.unit u " + "JOIN u.floor f "+ "JOIN f.building b " +
            "WHERE b.apartment.id = :apartmentId AND m.priority = :priority ")
    long countMaintenanceByApartmentAndPriority(@Param("apartmentId") UUID apartmentId, @Param("priority") Maintenance.Priority priority);

    //summary
    @Query("SELECT MONTH(m.requestedDate) AS month, COUNT(m) AS count, SUM(m.actualCost) AS totalCost " +
            "FROM Maintenance m " +
            "WHERE YEAR(m.requestedDate) = :year and m.unit.floor.building.apartment.id = :apartmentId " +
            "GROUP BY MONTH(m.requestedDate)")
    List<MaintenanceMonthlySummary> getMonthlySummary(@Param("year") int year,@Param("apartmentId")  UUID apartmentId);

    @Query("SELECT YEAR(m.requestedDate) AS year, COUNT(m) AS count, SUM(m.actualCost) AS totalCost " +
            "FROM Maintenance m " +
            "WHERE m.unit.floor.building.apartment.id = :apartmentId " +
            "GROUP BY YEAR(m.requestedDate) " +
            "ORDER BY YEAR(m.requestedDate) ASC")
    List<MaintenanceYearlySummary> getYearlySummary(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT DISTINCT YEAR(m.requestedDate) FROM Maintenance m WHERE m.unit.floor.building.apartment.id = :apartmentId ORDER BY YEAR(m.requestedDate) DESC")
    List<Integer> getAvailableYears(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT " +
            "YEAR(m.requestedDate) AS year, " +
            "COUNT(m) AS totalRequests, " +
            "COALESCE(SUM(m.actualCost), 0) AS totalCost, " +
            "SUM(CASE WHEN m.status = 'completed' THEN 1 ELSE 0 END) AS completed, " +
            "SUM(CASE WHEN m.status = 'pending' THEN 1 ELSE 0 END) AS pending " +
            "FROM Maintenance m " +
            "WHERE m.unit.floor.building.apartment.id = :apartmentId " +
            "GROUP BY YEAR(m.requestedDate) " +
            "ORDER BY YEAR(m.requestedDate) ASC")
    List<MaintenanceYearlySummaryProjection> getYearlySummaryTable(@Param("apartmentId") UUID apartmentId);


}