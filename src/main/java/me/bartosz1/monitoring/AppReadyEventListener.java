package me.bartosz1.monitoring;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppReadyEventListener {

    //ok ok I know time can be 0 but who rolls back their clock to 1970?
    private static long started = -1;

    public static long getStartedTimestamp() {
        return started;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setStartedTimestamp() {
        started = Instant.now().getEpochSecond();
    }
}
