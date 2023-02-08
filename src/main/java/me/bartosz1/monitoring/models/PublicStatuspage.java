package me.bartosz1.monitoring.models;

import java.util.ArrayList;
import java.util.List;

public class PublicStatuspage {

    private final String name;
    private final long id;
    private final StatuspageAnnouncement announcement;
    private final List<PublicMonitor> monitors = new ArrayList<>();
    public PublicStatuspage(Statuspage statuspage) {
        this.name = statuspage.getName();
        this.announcement = statuspage.getAnnouncement();
        this.id = statuspage.getId();
        statuspage.getMonitors().forEach(monitor -> monitors.add(new PublicMonitor(monitor)));
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public StatuspageAnnouncement getAnnouncement() {
        return announcement;
    }

    public List<PublicMonitor> getMonitors() {
        return monitors;
    }
}
