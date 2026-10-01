package vn.edu.crs.smartcommunity.identity.internal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.repository.RoleRepository;
import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;

/** Creates the one operational administrator when the configured email is absent. */
@Component
@Order(10)
@EnableConfigurationProperties(BootstrapAdminProperties.class)
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class BootstrapAdminInitializer implements CommandLineRunner {

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminInitializer(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(properties.email())) {
            return;
        }
        if (properties.initialPassword() == null || properties.initialPassword().isBlank()) {
            throw new IllegalStateException("app.bootstrap-admin.initial-password must be configured");
        }

        User user = new User();
        user.setFullName(properties.fullName());
        user.setEmail(properties.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(properties.initialPassword()));
        user.setActive(true);
        user.getRoles().add(roleRepository.findByName(RoleName.ADMIN).orElseThrow());
        userRepository.save(user);
    }
}
