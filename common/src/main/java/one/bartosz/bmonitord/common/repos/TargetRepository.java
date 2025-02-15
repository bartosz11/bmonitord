package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.target.Target;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TargetRepository extends R2dbcRepository<Target, UUID> {

    @Query("select * from targets where paused = false and deleted_at is null")
    Flux<Target> findAllNotPaused();
}
