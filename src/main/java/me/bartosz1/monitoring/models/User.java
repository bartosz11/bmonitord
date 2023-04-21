package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
@JsonIncludeProperties({"id", "username", "enabled", "lastUpdated"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private boolean enabled;
    private long lastUpdated;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<Monitor> monitors;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<Statuspage> statuspages;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<Notification> notifications;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<WhiteLabelDomain> whiteLabelDomains;

    @Override
    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    //Spring's bullshit
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public Set<Monitor> getMonitors() {
        return monitors;
    }

    public User setMonitors(Set<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public long getId() {
        return id;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public User setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }

    public Set<Statuspage> getStatuspages() {
        return statuspages;
    }

    public User setStatuspages(Set<Statuspage> statuspages) {
        this.statuspages = statuspages;
        return this;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public User setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public Set<WhiteLabelDomain> getWhiteLabelDomains() {
        return whiteLabelDomains;
    }

    public User setWhiteLabelDomains(Set<WhiteLabelDomain> whiteLabelDomains) {
        this.whiteLabelDomains = whiteLabelDomains;
        return this;
    }
}
