package me.bartosz1.monitoring.models.monitor;

//Monitor Creation Data Object
public class MonitorCDO {

    private int timeout;
    private String name;
    private String host;
    private MonitorType type;
    private int retries;
    private boolean verifySSL;
    private String allowedHttpCodes;
    private boolean isPublic;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public MonitorType getType() {
        return type;
    }

    public void setType(MonitorType type) {
        this.type = type;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean getVerifySSL() {
        return verifySSL;
    }

    public void setVerifySSL(boolean verifySSL) {
        this.verifySSL = verifySSL;
    }

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }

    public void setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
