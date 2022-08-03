package me.bartosz1.monitoring.models.statuspage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.monitor.Monitor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "statuspages")
public class Statuspage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //dunno if this annotation is a good solution
    @Fetch(FetchMode.JOIN)
    @ManyToMany
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Monitor> monitors;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonValue
    private List<StatuspageMonitorObject> statuspageMonitorObjects = new ArrayList<>();
    @OneToOne
    private StatuspageAnnouncement announcement;
    private String name;
    @ManyToOne
    @JsonIgnore
    private User user;

    public long getId() {
        return id;
    }
    @JsonProperty("monitor")
    public List<Monitor> getMonitors() {
        return monitors;
    }

    public Statuspage setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public StatuspageAnnouncement getAnnouncement() {
        return announcement;
    }

    public Statuspage setAnnouncement(StatuspageAnnouncement announcement) {
        this.announcement = announcement;
        return this;
    }

    public String getName() {
        return name;
    }

    public Statuspage setName(String name) {
        this.name = name;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Statuspage setUser(User user) {
        this.user = user;
        return this;
    }
    @JsonProperty("monitors")
    public List<StatuspageMonitorObject> getStatuspageMonitorObjects() {
        return statuspageMonitorObjects;
    }

    public Statuspage setStatuspageMonitorObjects(List<StatuspageMonitorObject> statuspageMonitorObjects) {
        this.statuspageMonitorObjects = statuspageMonitorObjects;
        return this;
    }
}
