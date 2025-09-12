package com.rentora.api.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "apartments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false,length = 100)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "tax_id", length = 13)
    private String taxId;

    @Column(name = "payment_due_day")
    private Integer paymentDueDay;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private BigDecimal lateFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_fee_type", length = 10)
    private LateFeeType lateFeeType; // "fixed" or "percentage"

    public enum LateFeeType {
        FIXED,PERCENTAGE
    }

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "country", length = 50)
    private String country = "Thailand";

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Bangkok";

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ApartmentUser> apartmentUsers;

    @Column(name = "currency", length = 3)
    private String currency = "THB";

    // created_by_user_id (FK to users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "id")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ApartmentStatus status = ApartmentStatus.SETUP_INCOMPLETE; // setup_incomplete, setup_in_progress, active, inactive

    public enum ApartmentStatus {
        SETUP_INCOMPLETE,SETUP_IN_PROGRESS,ACTIVE,INACTIVE
    }

    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}