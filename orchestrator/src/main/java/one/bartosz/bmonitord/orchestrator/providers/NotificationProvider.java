package one.bartosz.bmonitord.orchestrator.providers;

import one.bartosz.bmonitord.common.model.Heartbeat;
import reactor.core.publisher.Mono;

public abstract class NotificationProvider {

    public abstract Mono<Heartbeat> sendNotification(String header, String body, String credentials);

    public abstract Mono<Void> sendTestNotification(String credentials);
}
