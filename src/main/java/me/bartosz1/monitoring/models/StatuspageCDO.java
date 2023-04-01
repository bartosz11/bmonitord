package me.bartosz1.monitoring.models;

import java.util.List;

public class StatuspageCDO {

    private String name;
    private List<Long> monitorIds;
    private String logoLink;
    private String logoRedirect;

    public String getName() {
        return name;
    }

    public StatuspageCDO setName(String name) {
        this.name = name;
        return this;
    }

    public List<Long> getMonitorIds() {
        return monitorIds;
    }

    public StatuspageCDO setMonitorIds(List<Long> monitorIds) {
        this.monitorIds = monitorIds;
        return this;
    }

    public String getLogoLink() {
        return logoLink;
    }

    public StatuspageCDO setLogoLink(String logoLink) {
        this.logoLink = logoLink;
        return this;
    }

    public String getLogoRedirect() {
        return logoRedirect;
    }

    public StatuspageCDO setLogoRedirect(String logoRedirect) {
        this.logoRedirect = logoRedirect;
        return this;
    }
}
