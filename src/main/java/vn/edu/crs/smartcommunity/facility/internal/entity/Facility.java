package vn.edu.crs.smartcommunity.facility.internal.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Locale;

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

import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;

@Entity
@Table(name = "facilities", uniqueConstraints = @UniqueConstraint(
        name = "uk_facilities_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "building_id")
    private Long buildingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FacilityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FacilityStatus status = FacilityStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean bookable = true;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        normalizeCode();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        normalizeCode();
        updatedAt = Instant.now();
    }

    private void normalizeCode() {
        if (code != null) {
            code = code.trim().toUpperCase(Locale.ROOT);
        }
    }
}
