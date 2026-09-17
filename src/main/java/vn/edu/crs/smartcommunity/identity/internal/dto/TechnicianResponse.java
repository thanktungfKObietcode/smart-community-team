package vn.edu.crs.smartcommunity.identity.internal.dto;

public record TechnicianResponse(
        Long userId,
        String fullName,
        String email,
        boolean active) {
}
