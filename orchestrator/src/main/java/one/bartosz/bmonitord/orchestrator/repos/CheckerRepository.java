package one.bartosz.bmonitord.orchestrator.repos;

import one.bartosz.bmonitord.orchestrator.model.Checker;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CheckerRepository extends R2dbcRepository<Checker, UUID> {

    @Query("select * from checkers where key = :key and deleted_at is null")
    Mono<Checker> findFirstByKey(String key);
}
