package one.bartosz.bmonitord.orchestrator.repos;

import one.bartosz.bmonitord.orchestrator.model.alarm.Alarm;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlarmRepository extends R2dbcRepository<Alarm, UUID> {
}
