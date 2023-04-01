package me.bartosz1.monitoring.models;

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
    private List<Monitor> monitors;
    @ManyToOne
    private User user;
    private String logoLink;
    private String logoRedirect;

    public Statuspage() {
    }

    public Statuspage(StatuspageCDO cdo, User user) {
        this.name = cdo.getName();
        this.user = user;
        this.logoLink = cdo.getLogoLink();
        this.logoRedirect = cdo.getLogoRedirect();
    }

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

    public String getLogoLink() {
        return logoLink;
    }

    public Statuspage setLogoLink(String logoLink) {
        this.logoLink = logoLink;
        return this;
    }

    public String getLogoRedirect() {
        return logoRedirect;
    }

    public Statuspage setLogoRedirect(String logoRedirect) {
        this.logoRedirect = logoRedirect;
        return this;
    }
}
