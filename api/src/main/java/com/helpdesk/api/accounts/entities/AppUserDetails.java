package com.helpdesk.api.accounts.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Immutable security principal built from a {@link User} while its role/privileges are loaded.
 * It deliberately holds no entity reference so it is safe to use outside a persistence context.
 *
 * <p>Authorities are the role as {@code ROLE_<NAME>} (for {@code hasRole}) followed by every
 * privilege as {@code RESOURCE:ACTION} (for {@code hasAuthority}), see {@link Privilege#toAuthority()}.
 */
public class AppUserDetails implements UserDetails {
    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final String roleName;
    private final Set<String> privileges;
    private final List<GrantedAuthority> authorities;
    private final Instant passwordChangedAt;
    private final boolean emailVerified;

    private AppUserDetails(UUID id, String username, String email, String password, boolean enabled,
                           boolean emailVerified, String roleName, Set<String> privileges,
                           Instant passwordChangedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.roleName = roleName;
        this.passwordChangedAt = passwordChangedAt;
        this.privileges = Collections.unmodifiableSet(privileges);

        List<GrantedAuthority> granted = new ArrayList<>();
        if (roleName != null) {
            granted.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase(Locale.ROOT)));
        }
        privileges.forEach(privilege -> granted.add(new SimpleGrantedAuthority(privilege)));
        this.authorities = Collections.unmodifiableList(granted);
    }

    /** Must be called while {@code user.role.privileges} can still be lazily loaded. */
    public static AppUserDetails from(User user) {
        Role role = user.getRole();
        boolean roleActive = role != null && !Boolean.TRUE.equals(role.getIsDeleted());

        Set<String> privileges = new TreeSet<>();
        if (roleActive) {
            role.getPrivileges().stream()
                    .filter(privilege -> !Boolean.TRUE.equals(privilege.getIsDeleted()))
                    .map(Privilege::toAuthority)
                    .forEach(privileges::add);
        }

        return new AppUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                !Boolean.TRUE.equals(user.getIsDeleted()),
                // Not mapped to isEnabled(): Spring checks that BEFORE the password, which would reveal
                // to anyone knowing a username that the account is unverified. AuthService checks it
                // after a successful password match instead.
                Boolean.TRUE.equals(user.getEmailVerified()),
                roleActive ? role.getName() : null,
                privileges,
                user.getPasswordChangedAt() == null ? null
                        : user.getPasswordChangedAt().atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    /** Instant of the last password change, or {@code null} if never changed. */
    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
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
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }
}
