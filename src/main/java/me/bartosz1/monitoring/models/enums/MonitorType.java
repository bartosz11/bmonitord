package me.bartosz1.monitoring.models.enums;

import me.bartosz1.monitoring.providers.check.AgentCheckProvider;
import me.bartosz1.monitoring.providers.check.CheckProvider;
import me.bartosz1.monitoring.providers.check.HTTPCheckProvider;
import me.bartosz1.monitoring.providers.check.PingCheckProvider;

public enum MonitorType {

    HTTP(new HTTPCheckProvider(), true), PING(new PingCheckProvider(), true), AGENT(new AgentCheckProvider(), false);

    private final CheckProvider checkProvider;
    private final boolean applyRetriesLogic;

    MonitorType(CheckProvider checkProvider, boolean applyRetriesLogic) {
        this.checkProvider = checkProvider;
        this.applyRetriesLogic = applyRetriesLogic;
    }

    public CheckProvider getCheckProvider() {
        return checkProvider;
    }

    public boolean applyRetriesLogic() {
        return applyRetriesLogic;
    }
}
