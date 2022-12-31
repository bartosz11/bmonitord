package me.bartosz1.monitoring.models.enums;

import me.bartosz1.monitoring.providers.check.AgentCheckProvider;
import me.bartosz1.monitoring.providers.check.CheckProvider;
import me.bartosz1.monitoring.providers.check.HTTPCheckProvider;
import me.bartosz1.monitoring.providers.check.PingCheckProvider;

public enum MonitorType {

    HTTP(new HTTPCheckProvider()), PING(new PingCheckProvider()), AGENT(new AgentCheckProvider());

    private final CheckProvider checkProvider;

    MonitorType(CheckProvider checkProvider) {
        this.checkProvider = checkProvider;
    }

    public CheckProvider getCheckProvider() {
        return checkProvider;
    }
}
