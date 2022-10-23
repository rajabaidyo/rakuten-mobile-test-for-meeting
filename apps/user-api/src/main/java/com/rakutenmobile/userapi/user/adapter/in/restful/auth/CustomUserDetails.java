package com.rakutenmobile.userapi.user.adapter.in.restful.auth;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class CustomUserDetails implements UserDetails {

    private Collection<? extends GrantedAuthority> authorities;

    private String name;

    private String password;

    private String userId;

    private java.time.OffsetDateTime createdAt;

    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private boolean credentialsNonExpired;

    public CustomUserDetails(String name, String userId, String password,
                             java.time.OffsetDateTime createdAt,
                             Collection<? extends GrantedAuthority> authorities) {
            this.name = name;
            this.enabled = true;
            this.userId = userId;
            this.createdAt = createdAt;
            this.password = password;
            this.accountNonExpired = true;
            this.accountNonLocked = true;
            this.credentialsNonExpired = true;
            this.authorities = authorities;
        }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String stringAuthorities() {
        return String.join(",", authorities.stream().map(a -> a.getAuthority()).collect(Collectors.toList()));
    }
}
