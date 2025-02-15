package one.bartosz.bmonitord.common.model.target;

import one.bartosz.bmonitord.common.model.GenericEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("target_ping_info")
public class TargetPingInfo extends GenericEntity<TargetPingInfo> {

    private String host;
    private int timeout;
    private UUID targetId;
    @Transient
    private Target target;

    public String getHost() {
        return host;
    }

    public TargetPingInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public TargetPingInfo setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public TargetPingInfo setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public TargetPingInfo setTarget(Target target) {
        this.target = target;
        return this;
    }
}
