package one.bartosz.bmonitord.common.model;

import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("heartbeats")
public class Heartbeat {

    @Id
    private UUID id;
    private Instant timestamp;
    private long latency;
    private TargetStatus status;
    private UUID targetId;
    @Transient
    private Target target;
    private UUID checkerId;
    @Transient
    private Checker checker;

    public UUID getId() {
        return id;
    }

    public Heartbeat setId(UUID id) {
        this.id = id;
        return this;
    }

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

    public TargetStatus getStatus() {
        return status;
    }

    public Heartbeat setStatus(TargetStatus status) {
        this.status = status;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Heartbeat setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Heartbeat setTarget(Target target) {
        this.target = target;
        return this;
    }

    public UUID getCheckerId() {
        return checkerId;
    }

    public Heartbeat setCheckerId(UUID checkerId) {
        this.checkerId = checkerId;
        return this;
    }

    public Checker getChecker() {
        return checker;
    }

    public Heartbeat setChecker(Checker checker) {
        this.checker = checker;
        return this;
    }
}
