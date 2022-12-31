package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

public abstract class CheckProvider {
    //Returns MonitorStatus so we can compare it later with no problems
    //will be saved to database by some service using these providers
    public abstract MonitorStatus check(Monitor monitor);
}
