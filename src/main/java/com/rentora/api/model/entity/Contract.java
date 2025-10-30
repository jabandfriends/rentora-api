package com.rentora.api.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "contract_number", unique = true, length = 50)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonBackReference
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_user_id")
    private User tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false)
    private RentalType rentalType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rental_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentalPrice;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "advance_payment_months")
    private Integer advancePaymentMonths = 0;

    @Column(name = "late_fee_amount", precision = 10, scale = 2)
    private BigDecimal lateFeeAmount;

    @Column(name = "utilities_included")
    private Boolean utilitiesIncluded = false;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.active;

    @Column(name = "auto_renewal")
    private Boolean autoRenewal = false;

    @Column(name = "renewal_notice_days")
    private Integer renewalNoticeDays = 30;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminated_by_user_id")
    private User terminatedByUser;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name="electricity_meter_start_reading" ,nullable = false)
    private BigDecimal electricityMeterStartReading;

    @Column(name = "water_meter_start_reading", nullable = false)
    private BigDecimal waterMeterStartReading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RentalType {
        daily,monthly,yearly
    }

    public enum ContractStatus {
        draft,active,terminated,expired,renewed
    }
}
