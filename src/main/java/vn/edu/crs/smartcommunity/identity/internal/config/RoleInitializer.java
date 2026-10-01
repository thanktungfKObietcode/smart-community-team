package vn.edu.crs.smartcommunity.identity.internal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.identity.internal.entity.Role;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.repository.RoleRepository;

@Component
@Order(0)
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }
}
