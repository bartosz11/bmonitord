package one.bartosz.bmonitord.orchestrator;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.target.Target;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

public class StatusProcessingTask {

    private Target target;
    private List<UUID> checkers;
    private List<UUID> unreachableCheckers;
    private List<UUID> checkersDone;
    private Heartbeat decisiveHeartbeat;
    private Sinks.Many<Heartbeat> heartbeats = Sinks.many().unicast().onBackpressureBuffer();

    public Target getTarget() {
        return target;
    }

    public StatusProcessingTask setTarget(Target target) {
        this.target = target;
        return this;
    }

    public List<UUID> getCheckers() {
        return checkers;
    }

    public StatusProcessingTask setCheckers(List<UUID> checkers) {
        this.checkers = checkers;
        return this;
    }

    public List<UUID> getUnreachableCheckers() {
        return unreachableCheckers;
    }

    public StatusProcessingTask setUnreachableCheckers(List<UUID> unreachableCheckers) {
        this.unreachableCheckers = unreachableCheckers;
        return this;
    }

    public List<UUID> getCheckersDone() {
        return checkersDone;
    }

    public StatusProcessingTask setCheckersDone(List<UUID> checkersDone) {
        this.checkersDone = checkersDone;
        return this;
    }

    public Heartbeat getDecisiveHeartbeat() {
        return decisiveHeartbeat;
    }

    public StatusProcessingTask setDecisiveHeartbeat(Heartbeat decisiveHeartbeat) {
        this.decisiveHeartbeat = decisiveHeartbeat;
        return this;
    }

    public Sinks.Many<Heartbeat> getHeartbeats() {
        return heartbeats;
    }

    public StatusProcessingTask setHeartbeats(Sinks.Many<Heartbeat> heartbeats) {
        this.heartbeats = heartbeats;
        return this;
    }
}
