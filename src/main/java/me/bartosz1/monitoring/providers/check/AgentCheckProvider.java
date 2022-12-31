package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import java.time.Instant;

public class AgentCheckProvider extends CheckProvider {

    //Yes, it's that simple
    public MonitorStatus check(Monitor monitor) {
        if (monitor.getAgent().getLastDataReceived() + (long) monitor.getTimeout() < Instant.now().getEpochSecond()) {
            return MonitorStatus.DOWN;
        }
        return MonitorStatus.UP;
    }
}
