package one.bartosz.bmonitord.orchestrator.services;

import jakarta.annotation.PreDestroy;
import one.bartosz.bmonitord.orchestrator.BroadcastTask;
import one.bartosz.bmonitord.common.repos.OrchestratorRepository;
import one.bartosz.bmonitord.common.repos.SettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderElectionService {

    private static final long TAKEOVER_ATTEMPT_PERIOD = 5;
    //Random number
    private static final long LEADER_LOCK_ID = 245;
    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElectionService.class);

    private final DatabaseClient databaseClient;
    private final OrchestratorRepository orchestratorRepository;
    private final SettingRepository settingRepository;
    private final BroadcastTask broadcastTask;
    private final TaskScheduler taskScheduler;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private UUID selfId;
    private boolean isLeader = false;
    private ScheduledFuture<?> broadcastTaskFuture;

//    BroadcastTask is lazy to avoid circular dependence, it's also not always needed
    public LeaderElectionService(DatabaseClient databaseClient, OrchestratorRepository orchestratorRepository, SettingRepository settingRepository, @Lazy BroadcastTask broadcastTask, @Value("${one.bartosz.bmonitord.orchestrator.selfId}") String selfId, TaskScheduler taskScheduler) {
        this.databaseClient = databaseClient;
        this.orchestratorRepository = orchestratorRepository;
        this.settingRepository = settingRepository;
        this.broadcastTask = broadcastTask;
        this.taskScheduler = taskScheduler;
        try {
            this.selfId = UUID.fromString(selfId);
        } catch (IllegalArgumentException ignored) {
            LOGGER.error("Orchestrator doesn't have selfId!");
        }
    }

    //    Just so it doesn't crash because of absent tables
    @EventListener(ApplicationReadyEvent.class)
    private void startAttemptingTakeover() {
        if (selfId != null)
            scheduledExecutorService.scheduleAtFixedRate(this::attemptTakeover, 0, TAKEOVER_ATTEMPT_PERIOD, TimeUnit.SECONDS);
    }

    private void attemptTakeover() {
        databaseClient.sql("SELECT pg_try_advisory_lock($1)")
                .bind(0, LEADER_LOCK_ID)
                .map(row -> row.get(0, Boolean.class)).first()
                .subscribe(lockAcquired -> {
                    if (lockAcquired && !isLeader) {
                        orchestratorRepository.findFirstByLeader(true)
                                .flatMap(oldLeader -> {
                                    oldLeader.setLeader(false).markUpdated();
                                    return orchestratorRepository.save(oldLeader);
                                })
                                .switchIfEmpty(Mono.empty()) // In case there's no leader the entire chain doesn't fail
                                .then(orchestratorRepository.findById(selfId))
                                .flatMap(newLeader -> {
                                    newLeader.setLeader(true).markUpdated();
                                    return orchestratorRepository.save(newLeader);
                                })
                                .doOnSuccess(ignored -> {
                                    isLeader = true;
                                    startBroadcastingTask();
                                    LOGGER.info("Orchestrator took over leadership successfully!");
                                })
                                .subscribe();
                    }
                });
    }

    private void startBroadcastingTask() {
        settingRepository.findById("lastCheckStartedAt")
                .map(setting -> {
                    try {
                        return Long.parseLong(setting.getValue());
                    } catch (NumberFormatException ignored) {
                        return 0L;
                    }
                })
                .switchIfEmpty(Mono.just(0L))  // If no setting found, default to 0
                //IntellIJ doesn't like this chain of 3 .maps but I think this is more readable
                .map(lastCheckTimestamp -> Instant.now().getEpochSecond() - lastCheckTimestamp)  // Calculate elapsed time since last check
                .map(secondsSinceLastCheck ->
                        secondsSinceLastCheck >= 60 ? 0L : 60L - secondsSinceLastCheck
                )  // Determine that should be used
                .map(delay -> Instant.now().plusSeconds(delay))  // Convert delay to an instant
                .subscribe(initExecution -> {
                    //schedule the task, finally
                    broadcastTaskFuture = taskScheduler.scheduleAtFixedRate(
                            broadcastTask, initExecution, Duration.ofSeconds(60)
                    );
                });
    }

    //For graceful shutdowns
    @PreDestroy
    private void releaseOwnership() {
        databaseClient.sql("SELECT pg_advisory_unlock($1)")
                .bind(0, LEADER_LOCK_ID)
                .fetch().rowsUpdated()
                .doOnSuccess(rows -> {
                    if (rows > 0) {
                        LOGGER.info("Leadership lock has been successfully released.");
                    } else {
                        LOGGER.warn("Leadership lock was not released due to not being held.");
                    }
                })
                .doFinally(signal -> {
                    if (broadcastTaskFuture != null) {
                        broadcastTaskFuture.cancel(false);
                        LOGGER.info("Broadcast task has been successfully cancelled!");
                    }
                })
                .subscribe();
    }

    public boolean isLeader() {
        return isLeader;
    }
}
