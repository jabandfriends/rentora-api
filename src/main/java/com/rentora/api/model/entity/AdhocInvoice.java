package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "adhoc_invoices")
@Data
public class AdhocInvoice {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

        @Column(name = "adhoc_number", length = 50)
        private String adhocNumber;

        @Column(name = "apartment_id", nullable = false)
        private UUID apartmentId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "unit_id")
        private Unit unit;

        @Column(name = "tenant_user_id")
        private UUID tenantUserId;

        @Column(length = 200)
        private String title;

        @Column(columnDefinition = "text")
        private String description;

        @Column(length = 30)
        private String category;

        @Column(name = "final_amount", precision = 10, scale = 2)
        private BigDecimal finalAmount;

        @Column(name = "paid_amount", precision = 10, scale = 2)
        private BigDecimal paidAmount;

        @Column(name = "invoice_date")
        private LocalDate invoiceDate;

        @Column(name = "due_date")
        private LocalDate dueDate;

        @Column(name = "include_in_monthly")
        private Boolean includeInMonthly;

        @Column(name = "target_monthly_invoice_month")
        private LocalDate targetMonthlyInvoiceMonth;

        @Column(name = "monthly_invoice_id")
        private UUID monthlyInvoiceId;

        @Column(name = "included_at")
        private OffsetDateTime includedAt;

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_status", length = 20)
        private PaymentStatus paymentStatus = PaymentStatus.unpaid;

        @Column(name = "paid_at")
        private OffsetDateTime paidAt;

        @Column(name = "created_by_user_id")
        private UUID createdByUserId;

        @Column(name = "approved_by_user_id")
        private UUID approvedByUserId;

        @Column(name = "approved_at")
        private OffsetDateTime approvedAt;

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private InvoiceStatus status = InvoiceStatus.active;

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private InvoicePriority priority = InvoicePriority.normal;

        @Column(name = "receipt_urls", columnDefinition = "jsonb")
        private String receiptUrls; // JSON string

        @Column(name = "images", columnDefinition = "jsonb")
        private String images; // JSON string

        @Column(columnDefinition = "text")
        private String notes;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private OffsetDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private OffsetDateTime updatedAt;

        // --- ENUMs ---
        public enum PaymentStatus {
            paid, unpaid, overdue
        }

        public enum InvoiceStatus {
            active, inactive
        }

        public enum InvoicePriority {
            normal, high
        }
}
