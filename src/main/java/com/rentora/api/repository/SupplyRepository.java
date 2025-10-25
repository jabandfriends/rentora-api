package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface SupplyRepository extends JpaRepository<Supply, UUID>, JpaSpecificationExecutor<Supply> {

    long countByApartment(Apartment apartment);

    @Query("SELECT COUNT(s) FROM Supply s WHERE s.apartment = :apartment AND s.stockQuantity < s.minStock")
    long countLowStockByApartment(@Param("apartment") Apartment apartment);

    @Query("SELECT COALESCE(SUM(s.costPerUnit * s.stockQuantity), 0) FROM Supply s WHERE s.apartment = :apartment")
    BigDecimal totalCostSuppliesByApartment(Apartment apartment);
}
