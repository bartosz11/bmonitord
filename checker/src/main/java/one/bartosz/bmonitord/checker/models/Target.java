package one.bartosz.bmonitord.checker.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Target {

    @JsonProperty("ID")
    private long ID;
    @JsonProperty("Timeout")
    private int timeout;
    @JsonProperty("Type")
    private int type;
    @JsonProperty("HTTPInfo")
    private TargetHTTPInfo httpInfo;
    @JsonProperty("PingInfo")
    private TargetPingInfo pingInfo;

    public long getID() {
        return ID;
    }

    public Target setID(long ID) {
        this.ID = ID;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Target setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public TargetHTTPInfo getHTTPInfo() {
        return httpInfo;
    }

    public Target setHTTPInfo(TargetHTTPInfo HTTPInfo) {
        this.httpInfo = HTTPInfo;
        return this;
    }

    public TargetPingInfo getPingInfo() {
        return pingInfo;
    }

    public Target setPingInfo(TargetPingInfo pingInfo) {
        this.pingInfo = pingInfo;
        return this;
    }

    public int getType() {
        return type;
    }

    public Target setType(int type) {
        this.type = type;
        return this;
    }
}
