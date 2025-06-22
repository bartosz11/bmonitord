package one.bartosz.bmonitord.checker.providers;


import one.bartosz.bmonitord.checker.models.Heartbeat;
import one.bartosz.bmonitord.checker.models.Target;

public abstract class CheckProvider {

    public static CheckProvider getCheckProviderForType(int type) {
        return switch (type) {
            case 0 -> new PingCheckProvider();
            case 1 -> new HTTPCheckProvider();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public abstract Heartbeat check(Target target);
}
