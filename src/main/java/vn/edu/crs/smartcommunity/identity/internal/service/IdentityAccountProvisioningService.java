package vn.edu.crs.smartcommunity.identity.internal.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.ForbiddenException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.identity.api.IdentityAccountProvisioning;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.identity.api.ResidentAccountCommand;
import vn.edu.crs.smartcommunity.identity.api.StaffAccountCommand;
import vn.edu.crs.smartcommunity.identity.internal.entity.Role;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.repository.RoleRepository;
import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;

@Service
public class IdentityAccountProvisioningService implements IdentityAccountProvisioning {

    private static final Set<RoleName> STAFF_ROLES = Set.of(
            RoleName.MANAGER, RoleName.TECHNICIAN, RoleName.SECURITY);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public IdentityAccountProvisioningService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public IdentityUserInfo createStaffAccount(StaffAccountCommand command) {
        RoleName roleName = RoleName.valueOf(command.role().name());
        if (!STAFF_ROLES.contains(roleName)) {
            throw new ForbiddenException("Only MANAGER, TECHNICIAN and SECURITY accounts can be provisioned");
        }
        return createAccount(command.fullName(), command.email(), command.initialPassword(), roleName);
    }

    @Override
    @Transactional
    public IdentityUserInfo createResidentAccount(ResidentAccountCommand command) {
        return createAccount(command.fullName(), command.email(), command.initialPassword(), RoleName.RESIDENT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdentityUserInfo> listStaffAccounts() {
        return userRepository.findDistinctByRoles_NameInOrderByFullNameAsc(STAFF_ROLES).stream()
                .map(this::toInfo)
                .toList();
    }

    @Override
    @Transactional
    public IdentityUserInfo updateStaffAccountStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User account not found"));
        if (user.getRoles().stream().map(Role::getName).noneMatch(STAFF_ROLES::contains)) {
            throw new ForbiddenException("Only staff accounts can be activated or deactivated here");
        }
        user.setActive(active);
        return toInfo(user);
    }

    private IdentityUserInfo createAccount(String fullName, String email, String initialPassword, RoleName roleName) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(initialPassword));
        user.setActive(true);
        user.getRoles().add(roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Required role is not initialized")));
        return toInfo(userRepository.saveAndFlush(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private IdentityUserInfo toInfo(User user) {
        return new IdentityUserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toUnmodifiableSet()),
                user.isActive());
    }
}
