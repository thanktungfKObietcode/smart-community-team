package vn.edu.crs.smartcommunity.identity.internal.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import vn.edu.crs.smartcommunity.identity.internal.entity.User;

public final class ApplicationUserDetails implements UserDetails {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String password;
    private final boolean active;
    private final List<String> roles;
    private final List<GrantedAuthority> authorities;

    private ApplicationUserDetails(User user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.active = user.isActive();
        this.roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public static ApplicationUserDetails from(User user) {
        return new ApplicationUserDetails(user);
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
