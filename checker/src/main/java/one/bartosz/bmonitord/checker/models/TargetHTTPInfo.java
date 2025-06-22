package one.bartosz.bmonitord.checker.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TargetHTTPInfo {

    @JsonProperty("Host")
    private String host;
    // Split by a space
    @JsonProperty("AllowedCodes")
    private String allowedCodes;
    @JsonProperty("FollowRedirects")
    private boolean followRedirects;
    @JsonProperty("VerifySSLCert")
    private boolean verifySSLCert;

    public String getHost() {
        return host;
    }

    public TargetHTTPInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getAllowedCodes() {
        return allowedCodes;
    }

    public TargetHTTPInfo setAllowedCodes(String allowedCodes) {
        this.allowedCodes = allowedCodes;
        return this;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public TargetHTTPInfo setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public boolean isVerifySSLCert() {
        return verifySSLCert;
    }

    public TargetHTTPInfo setVerifySSLCert(boolean verifySSLCert) {
        this.verifySSLCert = verifySSLCert;
        return this;
    }

    @JsonIgnore
    public List<Integer> getAllowedCodesAsList() {
        return Arrays.stream(allowedCodes.split(" ")).map(Integer::parseInt).collect(Collectors.toList());
    }
}
