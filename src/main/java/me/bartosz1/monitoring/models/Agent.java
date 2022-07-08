package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.bartosz1.monitoring.models.monitor.Monitor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    private String id;
    @OneToOne(mappedBy = "agent")
    @JsonIgnore
    private Monitor monitor;
    private long lastDataReceived;
    //Other props start
    //Previously they were stored in InfluxDB, but they can't be visualized so what's the point?
    private String agentVersion;
    private String os;
    //ok, uptime could actually be visualized, but the graph would be useless
    private long uptime;
    private int cpuCores;
    private String cpuModel;
    private long ramTotal;
    private long swapTotal;
    private String ipAddress;

    public String getId() {
        return id;
    }

    public Agent setId(String id) {
        this.id = id;
        return this;
    }

    public long getLastDataReceived() {
        return lastDataReceived;
    }

    public Agent setLastDataReceived(long lastDataReceived) {
        this.lastDataReceived = lastDataReceived;
        return this;
    }

    public long getUptime() {
        return uptime;
    }

    public Agent setUptime(long uptime) {
        this.uptime = uptime;
        return this;
    }

    public String getOs() {
        return os;
    }

    public Agent setOs(String os) {
        this.os = os;
        return this;
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

    public long getSwapTotal() {
        return swapTotal;
    }

    public Agent setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
        return this;
    }

    public long getRamTotal() {
        return ramTotal;
    }

    public Agent setRamTotal(long ramTotal) {
        this.ramTotal = ramTotal;
        return this;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
