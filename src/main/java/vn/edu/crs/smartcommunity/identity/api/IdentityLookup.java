package vn.edu.crs.smartcommunity.identity.api;

import java.util.Optional;
import java.util.List;

public interface IdentityLookup {

    Optional<IdentityUserInfo> getUserById(Long userId);

    Optional<IdentityUserInfo> getUserByEmail(String email);

    boolean userExists(Long userId);

    List<IdentityUserInfo> findActiveTechnicians();
}
