package vn.edu.crs.smartcommunity.identity.api;

public record StaffAccountCommand(
        String fullName,
        String email,
        String initialPassword,
        StaffAccountRole role
) {
}
