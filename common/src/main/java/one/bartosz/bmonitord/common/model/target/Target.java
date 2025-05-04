package one.bartosz.bmonitord.common.model.target;

import one.bartosz.bmonitord.common.model.*;
import one.bartosz.bmonitord.common.model.alarm.Alarm;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table("targets")
public class Target extends GenericEntity<Target> {

    private String name;
    private long checksUp;
    private long checksDown;
    private int maxRetries;
    private int usedRetries;
    private Instant lastCheck;
    private TargetStatus lastStatus;
    private TargetType type;
    private boolean paused;
    private int timeout;

    private UUID userId;
    @Transient
    private User user;
    //These two are technically 1:1 relationships but still one entity "owns" another, so only the child holds the FK, I guess
    @Transient
    private TargetHTTPInfo targetHTTPInfo;
    @Transient
    private TargetPingInfo targetPingInfo;
    @Transient
    private List<Heartbeat> heartbeats;
    @Transient
    private List<Incident> incidents;
    @Transient
    private List<Alarm> alarms;
    @Transient
    private List<Checker> checkers;

    public String getName() {
        return name;
    }

    public Target setName(String name) {
        this.name = name;
        return this;
    }

    public long getChecksUp() {
        return checksUp;
    }

    public Target setChecksUp(long checksUp) {
        this.checksUp = checksUp;
        return this;
    }

    public long getChecksDown() {
        return checksDown;
    }

    public Target setChecksDown(long checksDown) {
        this.checksDown = checksDown;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Target setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public int getUsedRetries() {
        return usedRetries;
    }

    public Target setUsedRetries(int usedRetries) {
        this.usedRetries = usedRetries;
        return this;
    }

    public Instant getLastCheck() {
        return lastCheck;
    }

    public Target setLastCheck(Instant lastCheck) {
        this.lastCheck = lastCheck;
        return this;
    }

    public TargetStatus getLastStatus() {
        return lastStatus;
    }

    public Target setLastStatus(TargetStatus lastStatus) {
        this.lastStatus = lastStatus;
        return this;
    }

    public TargetType getType() {
        return type;
    }

    public Target setType(TargetType type) {
        this.type = type;
        return this;
    }

    public boolean isPaused() {
        return paused;
    }

    public Target setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public Target setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Target setUser(User user) {
        this.user = user;
        return this;
    }

    public TargetHTTPInfo getTargetHTTPInfo() {
        return targetHTTPInfo;
    }

    public Target setTargetHTTPInfo(TargetHTTPInfo targetHTTPInfo) {
        this.targetHTTPInfo = targetHTTPInfo;
        return this;
    }

    public TargetPingInfo getTargetPingInfo() {
        return targetPingInfo;
    }

    public Target setTargetPingInfo(TargetPingInfo targetPingInfo) {
        this.targetPingInfo = targetPingInfo;
        return this;
    }

    public List<Heartbeat> getHeartbeats() {
        return heartbeats;
    }

    public Target setHeartbeats(List<Heartbeat> heartbeats) {
        this.heartbeats = heartbeats;
        return this;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public Target setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
        return this;
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public Target setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        return this;
    }


    public List<Checker> getCheckers() {
        return checkers;
    }

    public Target setCheckers(List<Checker> checkers) {
        this.checkers = checkers;
        return this;
    }

    public Target incrementUsedRetries() {
        this.usedRetries++;
        return this;
    }

    public Target incrementChecks(TargetStatus targetStatus) {
        if (targetStatus == TargetStatus.UP) {
            this.checksUp++;
        } else {
            this.checksDown++;
        }
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Target setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
}
