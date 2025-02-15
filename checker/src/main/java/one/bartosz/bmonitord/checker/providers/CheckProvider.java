package one.bartosz.bmonitord.checker.providers;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetType;

public abstract class CheckProvider {

    public static CheckProvider getCheckProviderForType(TargetType type) {
        return switch (type) {
            case HTTP -> new HTTPCheckProvider();
            case PING -> new PingCheckProvider();
        };
    }

    public abstract Heartbeat check(Target target);
}
