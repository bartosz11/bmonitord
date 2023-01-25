package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "statuspages")
public class Statuspage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @OneToOne
    private StatuspageAnnouncement announcement;
    @ManyToMany
    @JsonIgnore
    private List<Monitor> monitors;
    @ManyToOne
    private User user;

    public long getId() {
        return id;
    }

    public Statuspage setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Statuspage setName(String name) {
        this.name = name;
        return this;
    }

    public StatuspageAnnouncement getAnnouncement() {
        return announcement;
    }

    public Statuspage setAnnouncement(StatuspageAnnouncement announcement) {
        this.announcement = announcement;
        return this;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public Statuspage setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Statuspage setUser(User user) {
        this.user = user;
        return this;
    }
}
