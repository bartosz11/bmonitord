package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.AppReadyEventListener;
import me.bartosz1.monitoring.models.Agent;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import java.time.Instant;

public class AgentCheckProvider extends CheckProvider {
    @Override
    public Heartbeat check(Monitor monitor) {
        Agent agent = monitor.getAgent();
        Heartbeat hb = new Heartbeat();
        long startedTimestamp = AppReadyEventListener.getStartedTimestamp();
        if (agent.isInstalled() && startedTimestamp != -1 && startedTimestamp + 300 < Instant.now().getEpochSecond()) {
            long epochSecond = Instant.now().getEpochSecond();
            if (monitor.getAgent().getLastDataReceived() + (long) monitor.getTimeout() < epochSecond) {
                hb.setStatus(MonitorStatus.DOWN);
            } else {
                hb.setStatus(MonitorStatus.UP);
            }
        }
        return hb;
    }
}
