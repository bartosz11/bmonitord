package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;


@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long startTimestamp;
    private long endTimestamp;
    private long duration;
    private boolean ongoing;
    @ManyToOne
    @JsonIncludeProperties({"id"})
    @JsonUnwrapped(prefix = "monitor")
    private Monitor monitor;

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
