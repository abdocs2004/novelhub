package com.novelhub.security;

import com.novelhub.entity.Role;
import com.novelhub.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security does not know our User entity. It works with the UserDetails interface.
 * This small class adapts our user to it, and it is the object you receive in controllers with
 * @AuthenticationPrincipal. Keeping it separate from the JPA entity avoids mixing security and persistence.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    private UserPrincipal(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // hasRole("ADMIN") in SecurityConfig looks for the authority "ROLE_ADMIN".
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; } // we log in with the email
}
