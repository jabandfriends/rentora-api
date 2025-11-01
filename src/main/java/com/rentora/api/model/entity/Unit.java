package com.rentora.api.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "units")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id",nullable = false)
    @JsonBackReference
    private Floor floor;

    @Column(name = "unit_name")
    private String unitName;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type")
    private UnitType unitType = UnitType.apartment;

    public enum UnitType {
        apartment,studio,penthouse,commercial
    }


    @Enumerated(EnumType.STRING)
    private UnitStatus status = UnitStatus.available;

    private String notes;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Contract> contracts;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public enum FurnishingStatus {
        unfurnished,furnished,semi_furnished
    }
    public enum UnitStatus {
        available,occupied,maintenance,reserved
    }

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Maintenance> unitMaintenance;
}
