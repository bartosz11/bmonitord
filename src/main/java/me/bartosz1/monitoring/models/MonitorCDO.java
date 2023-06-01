package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.models.monitor.MonitorHTTPInfoCDO;

public class MonitorCDO {

    private String name;
    private String host;
    private MonitorType type;

    private int retries;
    private int timeout;
    private boolean published;
    private MonitorHTTPInfoCDO httpInfoCDO;

    public String getName() {
        return name;
    }

    public MonitorCDO setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public MonitorCDO setHost(String host) {
        this.host = host;
        return this;
    }

    public MonitorType getType() {
        return type;
    }

    public MonitorCDO setType(MonitorType type) {
        this.type = type;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public MonitorCDO setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public MonitorCDO setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public MonitorCDO setPublished(boolean published) {
        this.published = published;
        return this;
    }
    @JsonProperty("httpInfo")
    public MonitorHTTPInfoCDO getHttpInfoCDO() {
        return httpInfoCDO;
    }

    public MonitorCDO setHttpInfoCDO(MonitorHTTPInfoCDO httpInfoCDO) {
        this.httpInfoCDO = httpInfoCDO;
        return this;
    }
}
