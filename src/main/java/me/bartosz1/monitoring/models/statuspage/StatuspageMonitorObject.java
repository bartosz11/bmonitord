package me.bartosz1.monitoring.models.statuspage;

import com.fasterxml.jackson.annotation.JsonInclude;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.monitor.MonitorStatus;
import me.bartosz1.monitoring.models.monitor.MonitorType;

import java.util.List;

//Don't ask why there's "Object" at the end of the name
//and why it is a separate class
//I know project structure is already complicated, I'll make it better at some point
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatuspageMonitorObject {

    private String name;
    private MonitorStatus lastStatus;
    private float uptime;
    private MonitorType type;
    private long createdAt;
    private int timeout;
    private List<Incident> lastIncidents;
    private List<Double> cpuUsage;
    private List<Double> ramUsage;

    public List<Double> getCpuUsage() {
        return cpuUsage;
    }

    public StatuspageMonitorObject setCpuUsage(List<Double> cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    public List<Double> getRamUsage() {
        return ramUsage;
    }

    public StatuspageMonitorObject setRamUsage(List<Double> ramUsage) {
        this.ramUsage = ramUsage;
        return this;
    }

    public List<Double> getDisksUsage() {
        return disksUsage;
    }

    public StatuspageMonitorObject setDisksUsage(List<Double> disksUsage) {
        this.disksUsage = disksUsage;
        return this;
    }

    public List<Double> getSwapUsage() {
        return swapUsage;
    }

    public StatuspageMonitorObject setSwapUsage(List<Double> swapUsage) {
        this.swapUsage = swapUsage;
        return this;
    }

    public List<Double> getIowait() {
        return iowait;
    }

    public StatuspageMonitorObject setIowait(List<Double> iowait) {
        this.iowait = iowait;
        return this;
    }

    public List<Long> getRx() {
        return rx;
    }

    public StatuspageMonitorObject setRx(List<Long> rx) {
        this.rx = rx;
        return this;
    }

    public List<Long> getTx() {
        return tx;
    }

    public StatuspageMonitorObject setTx(List<Long> tx) {
        this.tx = tx;
        return this;
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public StatuspageMonitorObject setTimestamps(List<Long> timestamps) {
        this.timestamps = timestamps;
        return this;
    }

    public List<Long> getResponseTime() {
        return responseTime;
    }

    public StatuspageMonitorObject setResponseTime(List<Long> responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    private List<Double> disksUsage;
    private List<Double> swapUsage;
    private List<Double> iowait;
    private List<Long> rx;
    private List<Long> tx;
    private List<Long> timestamps;
    private List<Long> responseTime;

    public String getName() {
        return name;
    }

    public StatuspageMonitorObject setName(String name) {
        this.name = name;
        return this;
    }

    public MonitorStatus getLastStatus() {
        return lastStatus;
    }

    public StatuspageMonitorObject setLastStatus(MonitorStatus lastStatus) {
        this.lastStatus = lastStatus;
        return this;
    }

    public float getUptime() {
        return uptime;
    }

    public StatuspageMonitorObject setUptime(float uptime) {
        this.uptime = uptime;
        return this;
    }

    public MonitorType getType() {
        return type;
    }

    public StatuspageMonitorObject setType(MonitorType type) {
        this.type = type;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public StatuspageMonitorObject setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public StatuspageMonitorObject setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public List<Incident> getLastIncidents() {
        return lastIncidents;
    }

    public StatuspageMonitorObject setLastIncidents(List<Incident> lastIncidents) {
        this.lastIncidents = lastIncidents;
        return this;
    }


}

