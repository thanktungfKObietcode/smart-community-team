package vn.edu.crs.smartcommunity.servicerequest.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, updatable = false)
    private String code;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "resident_user_id", nullable = false)
    private Long residentUserId;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceRequestPriority priority = ServiceRequestPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;

    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (code == null || code.isBlank()) {
            code = "REQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
