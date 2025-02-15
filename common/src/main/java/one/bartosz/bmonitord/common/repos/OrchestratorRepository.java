package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.Orchestrator;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrchestratorRepository extends R2dbcRepository<Orchestrator, UUID> {

    @Query("select * from orchestrators where leader = :leader and deleted_at is null")
    Mono<Orchestrator> findFirstByLeader(boolean leader);
}
