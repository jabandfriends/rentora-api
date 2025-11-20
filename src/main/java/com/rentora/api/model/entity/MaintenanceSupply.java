package com.rentora.api.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_supplies")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class MaintenanceSupply {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_request_id" , referencedColumnName = "id")
    private Maintenance maintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id",referencedColumnName = "id")
    private Supply supply;

    @Column(name = "quantity_used",nullable = false)
    private Integer quantityUsed;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}
