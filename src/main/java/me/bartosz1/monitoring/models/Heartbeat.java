package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import java.util.List;

@Entity
@Table(name = "heartbeats")
public class Heartbeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "monitor_id")
    @JsonIgnore
    private Monitor monitor;
    private int cpuFrequency;
    private float cpuUsage;
    private float ramUsage;
    private float swapUsage;
    private float disksUsage;
    private float iowait;
    private long rx;
    private long tx;
    private String diskData;
    private long responseTime;
    private MonitorStatus status;
    private long timestamp;

    public long getId() {
        return id;
    }

    public Heartbeat setId(long id) {
        this.id = id;
        return this;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public Heartbeat setMonitor(Monitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public int getCpuFrequency() {
        return cpuFrequency;
    }

    public Heartbeat setCpuFrequency(int cpuFrequency) {
        this.cpuFrequency = cpuFrequency;
        return this;
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public Heartbeat setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    public float getRamUsage() {
        return ramUsage;
    }

    public Heartbeat setRamUsage(float ramUsage) {
        this.ramUsage = ramUsage;
        return this;
    }

    public float getSwapUsage() {
        return swapUsage;
    }

    public Heartbeat setSwapUsage(float swapUsage) {
        this.swapUsage = swapUsage;
        return this;
    }

    public float getDisksUsage() {
        return disksUsage;
    }

    public Heartbeat setDisksUsage(float disksUsage) {
        this.disksUsage = disksUsage;
        return this;
    }

    public float getIowait() {
        return iowait;
    }

    public Heartbeat setIowait(float iowait) {
        this.iowait = iowait;
        return this;
    }

    public long getRx() {
        return rx;
    }

    public Heartbeat setRx(long rx) {
        this.rx = rx;
        return this;
    }

    public long getTx() {
        return tx;
    }

    public Heartbeat setTx(long tx) {
        this.tx = tx;
        return this;
    }

    public String getDiskData() {
        return diskData;
    }

    public Heartbeat setDiskData(String diskData) {
        this.diskData = diskData;
        return this;
    }

    public Heartbeat setDiskData(DiskData data) {
        this.diskData = data.toString();
        return this;
    }

    public Heartbeat setDiskData(List<DiskData> data) {
        this.diskData = DiskData.listToString(data);
        return this;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public Heartbeat setResponseTime(long responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public MonitorStatus getStatus() {
        return status;
    }

    public Heartbeat setStatus(MonitorStatus status) {
        this.status = status;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Heartbeat setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @JsonIgnore
    public DiskData getDiskDataObject() {
        return DiskData.fromString(diskData);
    }

    @JsonIgnore
    public List<DiskData> getDiskDataObjects() {
        return DiskData.multipleFromString(diskData);
    }
}