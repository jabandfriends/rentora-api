package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "invoice_number",unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonBackReference
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_user_id")
    private User tenant;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billEnd;

    @Column(name = "generation_month", nullable = false)
    private LocalDate genMonth;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "rental_amount", precision = 10, scale = 2)
    private BigDecimal rentAmount = BigDecimal.ZERO;

    @Column(name = "utilities_amount", precision = 10, scale = 2)
    private BigDecimal utilAmount = BigDecimal.ZERO;

    @Column(name = "services_amount", precision = 10, scale = 2)
    private BigDecimal serviceAmount = BigDecimal.ZERO;

    @Column(name = "late_fees_amount", precision = 10, scale = 2)
    private BigDecimal feesAmount = BigDecimal.ZERO;

    @Column(name = "discounts_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.unpaid;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = Boolean.TRUE;

    @Column(name = "pdf_url")
    private String pdf;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "viewed_at")
    private OffsetDateTime viewAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id")
    private User generatedByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public enum PaymentStatus {
        unpaid,
        paid,
        partially_paid,
        overdue,
        cancelled
    }
}
