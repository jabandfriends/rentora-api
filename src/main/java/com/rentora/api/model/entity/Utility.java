package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilities")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Utility {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Column(name = "utility_name",nullable = false)
    private String utilityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type",nullable = false)
    private UtilityType utilityType;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "fixed_price")
    private BigDecimal fixedPrice;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "minimum_charge")
    private BigDecimal minimumCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycle billingCycle = BillingCycle.monthly;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UtilityType {
        fixed,meter,tiered
    };
    public enum Category {
        utility,service,fee
    }
    public enum BillingCycle {
        monthly,quarterly,yearly;
    }
}
