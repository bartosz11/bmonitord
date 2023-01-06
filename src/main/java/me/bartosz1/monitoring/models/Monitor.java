package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "monitors")
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String host;
    private MonitorType type;
    private MonitorStatus lastStatus;
    private int retries;
    private int timeout;
    private long lastCheck;
    private long lastSuccessfulCheck;
    private long createdOn;
    private boolean paused;
    @JsonIgnore
    private int checksUp;
    @JsonIgnore
    private int checksDown;
    private String allowedHttpCodes;
    private boolean published;
    private boolean verifyCertificate;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    private List<Incident> incidents;
    @JsonIgnore
    @ManyToOne
    private User user;
    @JsonIgnore
    @ManyToMany
    private List<Notification> notifications;
    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Statuspage> statuspages;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    private Agent agent;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    private List<Heartbeat> heartbeats;

    public Monitor() {
    }

    public Monitor(MonitorCDO cdo, User user, Instant createdOn) {
        this.user = user;
        this.createdOn = createdOn.getEpochSecond();
        this.name = cdo.getName();
        this.host = cdo.getHost();
        this.type = cdo.getType();
        this.allowedHttpCodes = cdo.getAllowedHttpCodes();
        this.timeout = cdo.getTimeout();
        this.retries = cdo.getRetries();
    }

    public Monitor(MonitorCDO cdo, User user, Instant createdOn, Agent agent) {
        this.user = user;
        this.createdOn = createdOn.getEpochSecond();
        this.name = cdo.getName();
        this.host = cdo.getHost();
        this.type = cdo.getType();
        this.allowedHttpCodes = cdo.getAllowedHttpCodes();
        this.timeout = cdo.getTimeout();
        this.retries = cdo.getRetries();
        this.agent = agent;
    }


    public long getId() {
        return id;
    }

    public Monitor setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Monitor setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Monitor setHost(String host) {
        this.host = host;
        return this;
    }

    public MonitorType getType() {
        return type;
    }

    public Monitor setType(MonitorType type) {
        this.type = type;
        return this;
    }

    public MonitorStatus getLastStatus() {
        return lastStatus;
    }

    public Monitor setLastStatus(MonitorStatus lastStatus) {
        this.lastStatus = lastStatus;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public Monitor setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Monitor setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public Monitor setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
        return this;
    }

    public long getLastSuccessfulCheck() {
        return lastSuccessfulCheck;
    }

    public Monitor setLastSuccessfulCheck(long lastSuccessfulCheck) {
        this.lastSuccessfulCheck = lastSuccessfulCheck;
        return this;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public Monitor setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public boolean isPaused() {
        return paused;
    }

    public Monitor setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public int getChecksUp() {
        return checksUp;
    }

    public Monitor incrementChecksUp() {
        this.checksUp++;
        return this;
    }

    public int getChecksDown() {
        return checksDown;
    }

    public Monitor incrementChecksDown() {
        this.checksDown++;
        return this;
    }

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }

    public Monitor setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public Monitor setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isVerifyCertificate() {
        return verifyCertificate;
    }

    public Monitor setVerifyCertificate(boolean verifyCertificate) {
        this.verifyCertificate = verifyCertificate;
        return this;
    }

    public float getUptimePercent() {
        if (!(checksUp + checksDown == 0)) {
            return (float) (checksUp / (checksUp + checksDown)) * 100;
        } else return 0;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public Monitor setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Monitor setUser(User user) {
        this.user = user;
        return this;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public Monitor setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }

    public List<Statuspage> getStatuspages() {
        return statuspages;
    }

    public Monitor setStatuspages(List<Statuspage> statuspages) {
        this.statuspages = statuspages;
        return this;
    }

    public Agent getAgent() {
        return agent;
    }

    public Monitor setAgent(Agent agent) {
        this.agent = agent;
        return this;
    }

    @JsonIgnore
    public List<Integer> getAllowedHttpCodesAsList() {
        return Arrays.stream(allowedHttpCodes.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    public List<Heartbeat> getHeartbeats() {
        return heartbeats;
    }

    public Monitor setHeartbeats(List<Heartbeat> heartbeats) {
        this.heartbeats = heartbeats;
        return this;
    }
}
