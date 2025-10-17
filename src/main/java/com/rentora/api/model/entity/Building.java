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
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name= "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="apartment_id",nullable = false)
    @JsonBackReference
    private Apartment apartment;

    @Column(nullable = false , length = 100)
    private String name;

    private String description;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_type")
    private BuildingType buildingType = BuildingType.residential;

    @Enumerated(EnumType.STRING)
    private BuildingStatus status = BuildingStatus.active;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    List<Floor> floors;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BuildingStatus {
        active,inactive,maintenance
    }

    public enum BuildingType{
        residential,
    }
}
