package one.bartosz.bmonitord.orchestrator.repos;

import one.bartosz.bmonitord.orchestrator.model.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {
}
