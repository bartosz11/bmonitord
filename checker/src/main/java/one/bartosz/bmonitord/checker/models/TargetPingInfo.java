package one.bartosz.bmonitord.checker.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TargetPingInfo {

    @JsonProperty("Host")
    private String host;

    public String getHost() {
        return host;
    }

    public TargetPingInfo setHost(String host) {
        this.host = host;
        return this;
    }
}
