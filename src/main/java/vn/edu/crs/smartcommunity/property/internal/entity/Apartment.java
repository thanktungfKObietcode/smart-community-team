package vn.edu.crs.smartcommunity.property.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(
        name = "apartments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_apartments_building_unit",
                columnNames = {"building_id", "unit_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "unit_number", nullable = false, length = 30)
    private String unitNumber;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        normalizeUnitNumber();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        normalizeUnitNumber();
        updatedAt = Instant.now();
    }

    private void normalizeUnitNumber() {
        if (unitNumber != null) {
            unitNumber = unitNumber.trim().toUpperCase(Locale.ROOT);
        }
    }
}
