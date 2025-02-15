package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.alarm.Alarm;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AlarmRepository extends R2dbcRepository<Alarm, UUID> {

    Flux<Alarm> findAllByTargetId(UUID targetId);
}
