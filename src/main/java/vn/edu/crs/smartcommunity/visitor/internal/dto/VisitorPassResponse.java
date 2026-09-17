package vn.edu.crs.smartcommunity.visitor.internal.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPassStatus;

public record VisitorPassResponse(
        Long id,
        String code,
        String visitorName,
        String visitorPhone,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        VisitorPassStatus status,
        Instant createdAt,
        Instant checkedInAt,
        Instant checkedOutAt,
        ApartmentInfo apartment) {
}
