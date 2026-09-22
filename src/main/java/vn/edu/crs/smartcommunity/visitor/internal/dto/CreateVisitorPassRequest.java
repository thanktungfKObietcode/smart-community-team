package vn.edu.crs.smartcommunity.visitor.internal.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVisitorPassRequest(
        @NotBlank @Size(max = 150) String visitorName,
        @Size(max = 30) String visitorPhone,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil) {
}
