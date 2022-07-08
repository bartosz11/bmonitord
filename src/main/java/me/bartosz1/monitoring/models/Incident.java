package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import me.bartosz1.monitoring.models.monitor.Monitor;

import javax.persistence.*;

@Entity
@Table(name = "incidents")

public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JsonIncludeProperties({"id"})
    @JsonUnwrapped(prefix = "monitor")
    private Monitor monitor;
    private long startTimestamp;
    private long endTimestamp;
    private long duration;
    private boolean ongoing;

    public long getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public Incident setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
        return this;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public Incident setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public Incident setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public Incident setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
        return this;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public Incident setMonitor(Monitor monitor) {
        this.monitor = monitor;
        return this;
    }
}
