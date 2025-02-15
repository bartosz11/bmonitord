package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.Heartbeat;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HeartbeatRepository extends R2dbcRepository<Heartbeat, UUID> {
}
