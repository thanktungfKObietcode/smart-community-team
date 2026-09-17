package vn.edu.crs.smartcommunity.identity.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.smartcommunity.identity.internal.entity.Role;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}