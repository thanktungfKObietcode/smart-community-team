package vn.edu.crs.smartcommunity.resident.internal.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "residents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_residents_user_id",
                columnNames = "user_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resident_type", nullable = false, length = 30)
    private ResidentType residentType;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
