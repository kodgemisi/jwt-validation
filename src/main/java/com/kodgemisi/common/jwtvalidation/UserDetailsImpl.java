package com.kodgemisi.common.jwtvalidation;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetailsImpl implements UserDetails {


    private final Collection<? extends GrantedAuthority> authorities;

    private final String username;

    public UserDetailsImpl(Collection<String> authorities, String username) {

        authorities = authorities == null ? new ArrayList<>() : authorities;
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>(authorities.size());

        for (String authority: authorities) {
            grantedAuthorities.add((GrantedAuthority) () -> authority);
        }

        this.username = username;
        this.authorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }
}
