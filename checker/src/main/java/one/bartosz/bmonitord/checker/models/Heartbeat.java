package one.bartosz.bmonitord.checker.models;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class Heartbeat {

    @JsonProperty("Timestamp")
    private Instant timestamp;
    @JsonProperty("Status")
    private int status; // 0 means UP, 1 means DOWN
    @JsonProperty("Latency")
    private long latency;
    @JsonProperty("TargetID")
    private long targetID;
    @JsonProperty("CheckerID")
    private long checkerID;


    public Instant getTimestamp() {
        return timestamp;
    }

    public Heartbeat setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getLatency() {
        return latency;
    }

    public Heartbeat setLatency(long latency) {
        this.latency = latency;
        return this;
    }

    public long getTargetID() {
        return targetID;
    }

    public Heartbeat setTargetID(long targetID) {
        this.targetID = targetID;
        return this;
    }

    public long getCheckerID() {
        return checkerID;
    }

    public Heartbeat setCheckerID(long checkerID) {
        this.checkerID = checkerID;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Heartbeat setStatus(int status) {
        this.status = status;
        return this;
    }
}
