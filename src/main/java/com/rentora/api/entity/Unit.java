package com.rentora.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "units")
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

    private Integer bedrooms = 1;

    private BigDecimal bathrooms;

    @Column(name = "square_meters")
    private BigDecimal squareMeters;

    @Column(name = "balcony_count")
    private Integer balconyCount = 0;

    @Column(name = "parking_spaces")
    private Integer parkingSpaces = 0;

    @Enumerated(EnumType.STRING)
    private UnitStatus status = UnitStatus.available;

    @Enumerated(EnumType.STRING)
    @Column(name = "furnishing_status")
    private FurnishingStatus furnishingStatus;

    @Column(name = "floor_plan_url")
    private String floorPlan;

    private String notes;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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
}
