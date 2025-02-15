package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.Incident;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface IncidentRepository extends R2dbcRepository<Incident, UUID> {

    @Query("select * from incidents where target_id = :targetId order by start desc")
    Flux<Incident> findAllByTargetIdOrderByStartDesc(UUID targetId);
}
