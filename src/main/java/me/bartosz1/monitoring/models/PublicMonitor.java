package me.bartosz1.monitoring.models;

import me.bartosz1.monitoring.models.enums.MonitorStatus;

public class PublicMonitor {

    private final long id;
    private final String name;
    private final MonitorStatus lastStatus;
    private final boolean paused;
    private final long lastCheck;
    private final double uptime;

    public PublicMonitor(Monitor monitor) {
        this.id = monitor.getId();
        this.name = monitor.getName();
        this.lastStatus = monitor.getLastStatus();
        this.paused = monitor.isPaused();
        this.lastCheck = monitor.getLastCheck();
        this.uptime = monitor.getUptimePercent();
    }

    public String getName() {
        return name;
    }

    public MonitorStatus getLastStatus() {
        return lastStatus;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public long getId() {
        return id;
    }

    public double getUptime() {
        return uptime;
    }
}
