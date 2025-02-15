package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.alarm.AlarmNotification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlarmNotificationRepository extends R2dbcRepository<AlarmNotification, UUID> {

    Flux<AlarmNotification> findAllByAlarmId(UUID id);
}
