package me.bartosz1.monitoring.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "monitoring.prune.enabled", havingValue = "true", matchIfMissing = true)
public class PruneHeartbeatsAndIncidentsTask {

    private final DataSource dataSource;
    private final int pruneDays;
    private static final Logger LOGGER = LoggerFactory.getLogger(PruneHeartbeatsAndIncidentsTask.class);

    public PruneHeartbeatsAndIncidentsTask(DataSource dataSource, @Value("${monitoring.prune.age:14}") int pruneDays) {
        this.dataSource = dataSource;
        this.pruneDays = pruneDays;
    }

    @Scheduled(fixedDelayString = "${monitoring.prune.delay:86400}", timeUnit = TimeUnit.SECONDS)
    public void cleanup() {
        try {
            LOGGER.info("Starting prune of old heartbeats and incidents.");
            long min = Instant.now().getEpochSecond() - pruneDays * 86400L;
            Connection connection = dataSource.getConnection();
            LOGGER.debug("Using " + min + " as min timestamp");
            Statement deleteOrphansStmt = connection.createStatement();
            //this deletes orphans even though they shouldn't exist in the first place
            deleteOrphansStmt.execute("DELETE FROM incidents WHERE monitor_id = NULL;");
            deleteOrphansStmt.execute("DELETE FROM heartbeats WHERE monitor_id = NULL;");
            deleteOrphansStmt.close();
            //delete incidents older than X and not ongoing
            PreparedStatement purgeOldIncidents = connection.prepareStatement("DELETE FROM incidents WHERE id IN (SELECT id FROM incidents WHERE start_timestamp < ? AND ongoing = false)");
            purgeOldIncidents.closeOnCompletion();
            purgeOldIncidents.setLong(1, min);
            purgeOldIncidents.execute();
            //delete heartbeats older than X
            PreparedStatement purgeOldHbs = connection.prepareStatement("DELETE FROM heartbeats WHERE id IN (SELECT id FROM heartbeats WHERE timestamp < ?);");
            purgeOldHbs.closeOnCompletion();
            purgeOldHbs.setLong(1, min);
            purgeOldHbs.execute();
            LOGGER.info("Finished.");
        } catch (SQLException e) {
            LOGGER.error("SQLException while pruning DB!", e);
        }
    }
}
