package me.bartosz1.monitoring.models.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import me.bartosz1.monitoring.models.Monitor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "monitor_http_info")
public class MonitorHTTPInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String allowedHttpCodes;
    private boolean verifyCertificate;
    //private boolean followRedirects;
    @OneToOne
    @JsonIgnore
    private Monitor monitor;

    public MonitorHTTPInfo() {}

    public MonitorHTTPInfo(MonitorHTTPInfoCDO cdo, Monitor monitor) {
        this.allowedHttpCodes = cdo.getAllowedHttpCodes();
        this.verifyCertificate = cdo.isVerifyCertificate();
        this.monitor = monitor;
    }

    public long getId() {
        return id;
    }

    public String getAllowedHttpCodes() {
        return allowedHttpCodes;
    }

    public MonitorHTTPInfo setAllowedHttpCodes(String allowedHttpCodes) {
        this.allowedHttpCodes = allowedHttpCodes;
        return this;
    }

    public boolean isVerifyCertificate() {
        return verifyCertificate;
    }

    public MonitorHTTPInfo setVerifyCertificate(boolean verifyCertificate) {
        this.verifyCertificate = verifyCertificate;
        return this;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public MonitorHTTPInfo setMonitor(Monitor monitor) {
        this.monitor = monitor;
        return this;
    }

    @JsonIgnore
    public List<Integer> getAllowedHttpCodesAsList() {
        return Arrays.stream(allowedHttpCodes.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

}
