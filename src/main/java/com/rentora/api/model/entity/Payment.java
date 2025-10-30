package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_number", unique = true, length = 50)
    private String paymentNumber;

    // Invoice relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private ApartmentPayment paymentMethodEntity;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    // Users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id")
    private User paidByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_user_id")
    private User receivedByUser;

    // Dates
    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    // Status
    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    @Column(name = "verification_status", length = 20)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.pending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedByUser;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    // Attachments
    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(name = "slip_image_url", columnDefinition = "TEXT")
    private String slipImageUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Timestamps
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Enums
    public enum PaymentStatus {
        pending,completed,failed,refunded
    }

    public enum VerificationStatus {
        pending,verified,rejected
    }

}
