package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    // 🟢 1. Monthly revenue (for specific month)
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.invoice.apartment.id = :apartmentId
          AND p.paymentStatus = 'completed'
          AND p.paidAt >= :startOfMonth
          AND p.paidAt < :startOfNextMonth
    """)
    BigDecimal getMonthlyRevenueByApartment(
            @Param("apartmentId") UUID apartmentId,
            @Param("startOfMonth") OffsetDateTime startOfMonth,
            @Param("startOfNextMonth") OffsetDateTime startOfNextMonth
    );

    // 🟣 2. Total revenue (all time)
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.invoice.apartment.id = :apartmentId
          AND p.paymentStatus = 'completed'
    """)
    BigDecimal getTotalRevenueByApartment(@Param("apartmentId") UUID apartmentId);

    // 🔵 3. Pending payments
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.invoice.apartment.id = :apartmentId
          AND p.paymentStatus = 'pending'
    """)
    BigDecimal getPendingPaymentByApartment(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.invoice.apartment.id = :apartmentId")
    long countPaymentByApartment(@Param("apartmentId") UUID apartmentId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.invoice.apartment.id = :apartmentId AND p.paymentStatus = :paymentStatus")
    long countPaymentByApartmentIdAndStatus(@Param("apartmentId") UUID apartmentId,
                                            @Param("paymentStatus") Payment.PaymentStatus paymentStatus);
}