package me.bartosz1.monitoring.models;

import me.bartosz1.monitoring.models.enums.MonitorType;
public class MonitorCDO {

    private String name;
    private String host;
    private MonitorType type;

    private int retries;
    private int timeout;
    private String allowedHttpCodes;
    private boolean published;
    private boolean verifyCertificate;

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

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }

    public MonitorCDO setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public MonitorCDO setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isVerifyCertificate() {
        return verifyCertificate;
    }

    public MonitorCDO setVerifyCertificate(boolean verifyCertificate) {
        this.verifyCertificate = verifyCertificate;
        return this;
    }
}
