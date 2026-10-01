package vn.edu.crs.smartcommunity.identity.api;

public record ResidentAccountCommand(
        String fullName,
        String email,
        String initialPassword
) {
}
