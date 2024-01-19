package me.bartosz1.monitoring.tasks;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    @Transactional
    public void cleanup() {
        try {
            //not sure about unresponsiveness
            LOGGER.info("Starting prune of old heartbeats and incidents, app might become unresponsive. NEXT MESSAGE FROM THIS TASK DOESN'T INDICATE IT'S DONE");
            long min = Instant.now().getEpochSecond() - pruneDays * 86400L;
            Connection connection = dataSource.getConnection();
            LOGGER.debug("Using " + min + " as min timestamp");
            //get target incidents
            PreparedStatement incidentsSelectStatement = connection.prepareStatement("SELECT id FROM incidents WHERE start_timestamp < ? AND ongoing = false;");
            incidentsSelectStatement.setLong(1, min);
            incidentsSelectStatement.closeOnCompletion();
            List<Long> incidentIdsList = convertIdResultSetToIdList(incidentsSelectStatement.executeQuery());
            //get target heartbeats
            PreparedStatement heartbeatsSelectStatement = connection.prepareStatement("SELECT id FROM heartbeats WHERE timestamp < ?;");
            heartbeatsSelectStatement.setLong(1, min);
            heartbeatsSelectStatement.closeOnCompletion();
            List<Long> heartbeatIdsList = convertIdResultSetToIdList(heartbeatsSelectStatement.executeQuery());
            //delete all orphans first i guess
            Statement delStmt = connection.createStatement();
            delStmt.execute("DELETE FROM incidents WHERE monitor_id = NULL;");
            delStmt.execute("DELETE FROM heartbeats WHERE monitor_id = NULL;");
            if (!heartbeatIdsList.isEmpty()) {
                //delete from heartbeats
                String hbDeleteQuery = prepareEntityDeleteQuery(heartbeatIdsList, true);
                delStmt.execute(hbDeleteQuery);
            }
            if (!incidentIdsList.isEmpty()) {
                //delete from incidents
                String incDeleteQuery = prepareEntityDeleteQuery(incidentIdsList, false);
                delStmt.execute(incDeleteQuery);
            }
            //finished
            delStmt.close();
            LOGGER.info("At least " + heartbeatIdsList.size() + " heartbeats, " + incidentIdsList.size() + " incidents affected");
        } catch (SQLException e) {
            LOGGER.error("SQLException while pruning DB!", e);
        }
    }

    private List<Long> convertIdResultSetToIdList(ResultSet resultSet) throws SQLException {
        List<Long> ids = new ArrayList<>();
        while (resultSet.next()) {
            ids.add(resultSet.getLong("id"));
        }
        resultSet.close();
        return ids;
    }

    private String prepareEntityDeleteQuery(List<Long> ids, boolean heartbeat) {
        StringBuilder sb = new StringBuilder().append("DELETE FROM ").append(heartbeat ? "heartbeats" : "incidents").append(" WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            long id = ids.get(i);
            sb.append(id);
            if (ids.size() - 1 == i) sb.append(");");
            else sb.append(", ");
        }
        return sb.toString();
    }
}
