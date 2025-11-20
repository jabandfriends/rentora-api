package com.rentora.api.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "supplies")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id" , referencedColumnName = "id")
    private Apartment apartment;

    @Column(name = "name" , length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private SupplyCategory category;

    private String description;

    @Column(name = "unit" , length = 20,nullable = false)
    private String unit;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "min_stock")
    private Integer minStock = 5;

    @Column(name = "cost_per_unit")
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    private Boolean isDeleted =  Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


    public enum SupplyCategory{
        electrical,plumbing,cleaning,hvac,painting,general
    }

    public enum SupplyStockStatus{
        in_stock,out_of_stock,low_stock
    }

    //helper method
    public SupplyStockStatus getSupplyStockStatus(){
        if(this.stockQuantity == 0) return SupplyStockStatus.out_of_stock;
        if(stockQuantity<minStock) return SupplyStockStatus.low_stock;
        else {
           return SupplyStockStatus.in_stock;
        }

    }
}
