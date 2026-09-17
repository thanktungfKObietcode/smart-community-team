package vn.edu.crs.smartcommunity.identity.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByActiveTrueAndRoles_NameOrderByFullNameAsc(RoleName roleName);
}
