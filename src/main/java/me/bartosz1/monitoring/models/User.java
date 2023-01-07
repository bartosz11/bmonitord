package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIncludeProperties({"id", "username", "enabled"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private boolean enabled;
    private long lastUpdated;
    @OneToMany
    private List<Monitor> monitors;
    @OneToMany
    private List<Statuspage> statuspages;
    @OneToMany
    private List<Notification> notifications;

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

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public User setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public long getId() {
        return id;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public User setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }

    public List<Statuspage> getStatuspages() {
        return statuspages;
    }

    public User setStatuspages(List<Statuspage> statuspages) {
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
}
