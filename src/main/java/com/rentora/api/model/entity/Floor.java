package com.rentora.api.model.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "floors")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="building_id",nullable = false)
    @JsonBackReference
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name="floor_name")
    private String floorName;

    @Column(name = "total_units")
    private Integer totalUnits = 0;

    @Column(name = "floor_plan_url")
    private String floorPlanUrl;

    @OneToMany(mappedBy = "floor", fetch = FetchType.LAZY)
    List<Unit> units;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
