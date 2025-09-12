package com.rentora.api.security;

import com.rentora.api.entity.ApartmentUser;
import com.rentora.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {  // ✅ implement UserDetails
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isActive;
    private boolean isAccountLocked;
    private boolean mustChangePassword;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getApartmentUsers().stream()
                .filter(ApartmentUser::getIsActive)
                .map(apartmentUser -> new SimpleGrantedAuthority("ROLE_" + apartmentUser.getRole().name()))
                .collect(Collectors.toList());

        // Add default user role
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getIsActive(),
                user.isAccountLocked(),
                user.isCredentialsExpired(),  // assuming this maps to mustChangePassword
                authorities
        );
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // UserDetails interface methods
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !mustChangePassword;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
