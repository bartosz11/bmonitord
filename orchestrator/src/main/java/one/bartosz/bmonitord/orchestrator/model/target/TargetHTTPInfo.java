package one.bartosz.bmonitord.orchestrator.model.target;

import one.bartosz.bmonitord.orchestrator.model.GenericEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("target_http_info")
public class TargetHTTPInfo extends GenericEntity<TargetHTTPInfo> {

    private String host;
    //    Split by a space
    private String allowedCodes;
    private int timeout;
    private boolean followRedirects;
    private boolean verifySSLCertificate;
    private UUID targetId;
    @Transient
    private Target target;

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

    public int getTimeout() {
        return timeout;
    }

    public TargetHTTPInfo setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public TargetHTTPInfo setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public boolean isVerifySSLCertificate() {
        return verifySSLCertificate;
    }

    public TargetHTTPInfo setVerifySSLCertificate(boolean verifySSLCertificate) {
        this.verifySSLCertificate = verifySSLCertificate;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public TargetHTTPInfo setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public TargetHTTPInfo setTarget(Target target) {
        this.target = target;
        return this;
    }
}
