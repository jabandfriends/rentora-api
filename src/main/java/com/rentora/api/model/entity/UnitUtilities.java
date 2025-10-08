package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "unit_utilities")
@Data
public class UnitUtilities {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id", nullable = false)
    private Utility utility;

    @Column(name = "reading_date")
    private LocalDate readingDate;

    @Column(name = "meter_start")
    private BigDecimal meterStart;

    @Column(name = "meter_end")
    private BigDecimal meterEnd;

    @Column(name = "usage_amount")
    private BigDecimal usageAmount;

    @Column(name = "usage_month")
    private LocalDate usageMonth;

    @Column(name = "calculated_cost")
    private BigDecimal calculatedCost;

    @Column(name = "notes", length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "read_by_user_id")
    private User readByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}