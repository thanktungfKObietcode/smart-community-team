package vn.edu.crs.smartcommunity.identity.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.identity.api.IdentityAccountProvisioning;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.identity.api.StaffAccountCommand;
import vn.edu.crs.smartcommunity.identity.internal.dto.CreateStaffAccountRequest;
import vn.edu.crs.smartcommunity.identity.internal.dto.ManagedUserResponse;
import vn.edu.crs.smartcommunity.identity.internal.dto.UpdateAccountStatusRequest;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IdentityAccountProvisioning accountProvisioning;

    public AdminUserController(IdentityAccountProvisioning accountProvisioning) {
        this.accountProvisioning = accountProvisioning;
    }

    @PostMapping
    public ManagedUserResponse createStaffAccount(@Valid @RequestBody CreateStaffAccountRequest request) {
        return toResponse(accountProvisioning.createStaffAccount(new StaffAccountCommand(
                request.fullName(), request.email(), request.initialPassword(), request.role())));
    }

    @GetMapping
    public List<ManagedUserResponse> listStaffAccounts() {
        return accountProvisioning.listStaffAccounts().stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{userId}/active")
    public ManagedUserResponse updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        return toResponse(accountProvisioning.updateStaffAccountStatus(userId, request.active()));
    }

    private ManagedUserResponse toResponse(IdentityUserInfo user) {
        return new ManagedUserResponse(
                user.userId(), user.fullName(), user.email(), user.roles(), user.active());
    }
}
