package one.bartosz.bmonitord.common.model;

import one.bartosz.bmonitord.common.model.target.Target;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Table("incidents")
public class Incident {

    @Id
    private UUID id;
    private Instant start;
    private Instant end;
    private Duration duration;
    private boolean ongoing;
    private UUID targetId;
    @Transient
    private Target target;

    public UUID getId() {
        return id;
    }

    public Incident setId(UUID id) {
        this.id = id;
        return this;
    }

    public Instant getStart() {
        return start;
    }

    public Incident setStart(Instant start) {
        this.start = start;
        return this;
    }

    public Instant getEnd() {
        return end;
    }

    public Incident setEnd(Instant end) {
        this.end = end;
        return this;
    }

    public Duration getDuration() {
        return duration;
    }

    public Incident setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public Incident setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Incident setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Incident setTarget(Target target) {
        this.target = target;
        return this;
    }
}
