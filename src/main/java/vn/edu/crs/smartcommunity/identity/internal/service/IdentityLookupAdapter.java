package vn.edu.crs.smartcommunity.identity.internal.service;

import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;

@Service
public class IdentityLookupAdapter implements IdentityLookup {

    private final UserRepository userRepository;

    public IdentityLookupAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<IdentityUserInfo> getUserById(Long userId) {
        return userRepository.findById(userId).map(this::toInfo);
    }

    @Override
    public Optional<IdentityUserInfo> getUserByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmailIgnoreCase(normalizedEmail).map(this::toInfo);
    }

    @Override
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public List<IdentityUserInfo> findActiveTechnicians() {
        return userRepository.findByActiveTrueAndRoles_NameOrderByFullNameAsc(RoleName.TECHNICIAN)
                .stream()
                .map(this::toInfo)
                .toList();
    }

    private IdentityUserInfo toInfo(User user) {
        return new IdentityUserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toUnmodifiableSet()),
                user.isActive());
    }
}
