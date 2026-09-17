package vn.edu.crs.smartcommunity.identity.internal.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;

@Service
public class IdentityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public IdentityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username == null ? "" : username.trim();
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(ApplicationUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
    }
}
