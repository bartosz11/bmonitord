package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

public abstract class CheckProvider {
    //Returns Heartbeat, allows us to get both status and response time
    public abstract Heartbeat check(Monitor monitor);
}
