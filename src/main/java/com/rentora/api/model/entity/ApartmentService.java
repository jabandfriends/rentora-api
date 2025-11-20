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
@Table(name = "extra_services")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class ApartmentService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Column(name = "service_name", length = 50, nullable = false)
    private String serviceName;

    private String description;

    @Column(name = "price", nullable = false,precision = 10,scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type")
    private  BillingType billingType = BillingType.monthly;

    @Enumerated(EnumType.STRING)
    private Category category = Category.service;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;

    @Column(name = "max_quantity")
    private Integer maxQuantity = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Category {
        service,parking,pool,gym,security,general
    }

    public enum BillingType {
        monthly, one_time,daily,yearly;
    }
}
