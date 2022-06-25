package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String username;
    private String password;
    private boolean enabled;
    private String authorities;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Monitor> monitors;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AccessToken> accessTokens;

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    //Too complicated, I know
    //just wanted to avoid making another 10 tables in the DB and complete mess
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Arrays.stream(authorities.split("\\s+")).forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));
        return grantedAuthorities;
    }

    public String getAuthoritiesAsString() {
        return authorities;
    }

    public User setAuthorities(Collection<GrantedAuthority> grantedAuthorities) {
        StringBuilder sb = new StringBuilder();
        grantedAuthorities.forEach(grantedAuthority -> {
            sb.append(grantedAuthority.getAuthority()).append(" ");
        });
        this.authorities = sb.toString();
        return this;
    }
    public User setAuthoritiesAsString(String authorities) {
        this.authorities = authorities;
        return this;
    }
    //We don't use account expiry, so this will always return true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //We don't use this too
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //And this
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }

    public List<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    public void setAccessTokens(List<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }
}
