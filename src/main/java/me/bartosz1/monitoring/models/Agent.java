package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "agents")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String agentVersion;
    private int cpuCores;
    private String cpuModel;
    private String ipAddress;
    private long lastDataReceived;
    private String os;
    private long ramTotal;
    private long swapTotal;
    private int uptime;
    private boolean installed;
    //hides IP to unauthorized people, default false
    private boolean hideIP;
    @OneToOne
    @JsonIgnore
    private Monitor monitor;

    public Agent() {}

    public Agent(Agent a) {
        this.id = a.getId();
        this.agentVersion = a.getAgentVersion();
        this.cpuCores = a.getCpuCores();
        this.cpuModel= a.getCpuModel();
        this.ipAddress = a.getIpAddress();
        this.lastDataReceived = a.getLastDataReceived();
        this.os = a.getOs();
        this.ramTotal = a.getRamTotal();
        this.swapTotal = a.getSwapTotal();
        this.uptime = a.getUptime();
        this.installed = a.isInstalled();
        this.hideIP = a.isHideIP();
        this.monitor = a.getMonitor();
    }

    public UUID getId() {
        return id;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public Agent setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
        return this;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public Agent setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
        return this;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public Agent setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Agent setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public long getLastDataReceived() {
        return lastDataReceived;
    }

    public Agent setLastDataReceived(long lastDataReceived) {
        this.lastDataReceived = lastDataReceived;
        return this;
    }

    public String getOs() {
        return os;
    }

    public Agent setOs(String os) {
        this.os = os;
        return this;
    }

    public long getRamTotal() {
        return ramTotal;
    }

    public Agent setRamTotal(long ramTotal) {
        this.ramTotal = ramTotal;
        return this;
    }

    public long getSwapTotal() {
        return swapTotal;
    }

    public Agent setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
        return this;
    }

    public int getUptime() {
        return uptime;
    }

    public Agent setUptime(int uptime) {
        this.uptime = uptime;
        return this;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public Agent setMonitor(Monitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public boolean isInstalled() {
        return installed;
    }

    public Agent setInstalled(boolean installed) {
        this.installed = installed;
        return this;
    }

    public boolean isHideIP() {
        return hideIP;
    }

    public Agent setHideIP(boolean hideIP) {
        this.hideIP = hideIP;
        return this;
    }

    public Agent setId(UUID id) {
        this.id = id;
        return this;
    }
}
