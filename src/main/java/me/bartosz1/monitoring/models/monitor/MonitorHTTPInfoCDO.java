package me.bartosz1.monitoring.models.monitor;

public class MonitorHTTPInfoCDO {

    private String allowedHttpCodes;
    private boolean verifyCertificate;
    private boolean followRedirects;

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }

    public MonitorHTTPInfoCDO setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
        return this;
    }

    public boolean isVerifyCertificate() {
        return verifyCertificate;
    }

    public MonitorHTTPInfoCDO setVerifyCertificate(boolean verifyCertificate) {
        this.verifyCertificate = verifyCertificate;
        return this;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public MonitorHTTPInfoCDO setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }
}
