package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "supply_transactions")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class SupplyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_request_id" , referencedColumnName = "id")
    private Maintenance maintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id",referencedColumnName = "id")
    private Supply supply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_user_id",referencedColumnName = "id")
    private ApartmentUser apartmentUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private SupplyTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "number_type")
    private SupplyTransactionNumberType numberType = SupplyTransactionNumberType.positive;

    @Column(name = "quantity" , nullable = false)
    private Integer quantity;

    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum SupplyTransactionType {
        purchase,use,adjustment
    }
    public enum SupplyTransactionNumberType {
        negative,positive
    }


}
