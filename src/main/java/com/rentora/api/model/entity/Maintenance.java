package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "ticket_number", length = 50)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id" , referencedColumnName = "id")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_user_id" , referencedColumnName = "id")
    private User tenantUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id" , referencedColumnName = "id")
    private User assignedToUser;

    @Column(name = "title" , length = 100, nullable = false)
    private String title;

    @Column(name = "description" , columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category" , length = 30)
    private Category category;
    public enum Category {
        general,plumbing,electrical,hvac
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status" , length = 20)
    private Status status;
    public enum Status {
        pending,assigned,in_progress,completed,cancelled
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "priority" , length = 20)
    private Priority priority;
    public enum Priority {
        low,normal,high,urgent
    }

    @Column(name = "requested_date")
    private java.time.LocalDate requestedDate;

    @Column(name = "appointment_date")
    private java.time.OffsetDateTime appointmentDate;

    @Column(name = "started_at")
    private java.time.OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private java.time.OffsetDateTime completedAt;

    @Column(name = "due_date")
    private java.time.OffsetDateTime dueDate;

    @Column(name = "estimated_hours", precision = 4, scale = 1)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 4, scale = 1)
    private BigDecimal actualHours;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "work_summary", columnDefinition = "text")
    private String workSummary;

    @Column(name = "tenant_feedback", columnDefinition = "text")
    private String tenantFeedback;

    @Column(name = "tenant_rating")
    private Integer tenantRating;

    @Column(name = "is_emergency")
    private Boolean isEmergency = false;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "recurring_schedule", length = 20)
    private String recurringSchedule;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
