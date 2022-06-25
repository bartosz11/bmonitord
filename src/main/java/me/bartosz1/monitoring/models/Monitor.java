package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
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
    private String type;
    private String host;
    private int timeout;
    private int retries;
    private boolean verifySSLCerts;
    private MonitorStatus lastStatus;
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContactList> contactLists;
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    private List<Incident> incidents;
    @ManyToOne
    @JsonIgnore
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "agent_id")
    private Agent agent;
    //easier to store as string
    private String allowedHttpCodes;

    public Monitor() {}

    public Monitor(MonitorCDO cdo, User user) {
        this.name = cdo.getName();
        this.host = cdo.getHost();
        this.timeout = cdo.getTimeout();
        this.type = cdo.getType();
        this.verifySSLCerts = cdo.getVerifySSL();
        this.retries = cdo.getRetries();
        this.allowedHttpCodes = cdo.getAllowedHttpCodes();
        this.user = user;
    }

    public Monitor(MonitorCDO cdo, User user, Agent agent) {
        this.name = cdo.getName();
        this.host = cdo.getHost();
        this.timeout = cdo.getTimeout();
        this.type = cdo.getType();
        this.verifySSLCerts = cdo.getVerifySSL();
        this.retries = cdo.getRetries();
        this.allowedHttpCodes = cdo.getAllowedHttpCodes();
        this.user = user;
        this.agent = agent;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Monitor setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Monitor setType(String type) {
        this.type = type;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Monitor setHost(String host) {
        this.host = host;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Monitor setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public Monitor setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public boolean getVerifySSL() {
        return verifySSLCerts;
    }

    public Monitor setVerifySSLCerts(boolean verifySSLCerts) {
        this.verifySSLCerts = verifySSLCerts;
        return this;
    }

    public MonitorStatus getLastStatus() {
        return lastStatus;
    }

    public Monitor setLastStatus(MonitorStatus lastStatus) {
        this.lastStatus = lastStatus;
        return this;
    }

    public List<ContactList> getContactLists() {
        return contactLists;
    }

    public Monitor setContactLists(List<ContactList> contactLists) {
        this.contactLists = contactLists;
        return this;
    }

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }
    @JsonIgnore
    public List<Integer> getAllowedHttpCodesAsList() {
        return Arrays.stream(allowedHttpCodes.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    public Monitor setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
        return this;
    }

    public Monitor setAllowedHttpCodes(List<Integer> allowedHttpCodes) {
        StringBuilder sb = new StringBuilder();
        allowedHttpCodes.forEach(code -> sb.append(code).append(","));
        this.allowedHttpCodes = sb.toString();
        return this;
    }

    public Agent getAgent() {
        return agent;
    }

    public Monitor setAgent(Agent agent) {
        this.agent = agent;
        return this;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
