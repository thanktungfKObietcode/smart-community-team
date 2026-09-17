package vn.edu.crs.smartcommunity.identity.internal.config;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.identity.internal.entity.Role;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.repository.RoleRepository;
import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;

@Component
@Order(0)
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private static final String DEVELOPMENT_PASSWORD = "123456";
    private static final Map<RoleName, DemoAccount> DEMO_ACCOUNTS = Map.of(
            RoleName.ADMIN, new DemoAccount("admin@test.com", "Quản trị viên Demo"),
            RoleName.MANAGER, new DemoAccount("manager@test.com", "Ban quản lý Demo"),
            RoleName.RESIDENT, new DemoAccount("resident@test.com", "Cư dân Demo"),
            RoleName.TECHNICIAN, new DemoAccount("technician@test.com", "Kỹ thuật viên Demo"),
            RoleName.SECURITY, new DemoAccount("security@test.com", "Bảo vệ Demo"));

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        DEMO_ACCOUNTS.forEach(this::createAccountIfMissing);
    }

    private void createAccountIfMissing(RoleName roleName, DemoAccount account) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        if (userRepository.existsByEmailIgnoreCase(account.email())) {
            return;
        }

        User user = new User();
        user.setFullName(account.fullName());
        user.setEmail(account.email());
        user.setPassword(passwordEncoder.encode(DEVELOPMENT_PASSWORD));
        user.setActive(true);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private record DemoAccount(String email, String fullName) {
    }
}
