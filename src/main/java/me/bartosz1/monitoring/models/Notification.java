package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String credentials;
    private NotificationType type;
    @ManyToMany
    private List<Monitor> monitors;
    @ManyToOne
    private User user;

    public Notification(NotificationCDO cdo, User user) {
        this.user = user;
        this.name = cdo.getName();
        this.credentials = cdo.getCredentials();
        this.type = cdo.getType();
    }

    public Notification() {
    }

    public long getId() {
        return id;
    }

    public Notification setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Notification setName(String name) {
        this.name = name;
        return this;
    }

    public String getCredentials() {
        return credentials;
    }

    public Notification setCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public Notification setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public Notification setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Notification setUser(User user) {
        this.user = user;
        return this;
    }
}
