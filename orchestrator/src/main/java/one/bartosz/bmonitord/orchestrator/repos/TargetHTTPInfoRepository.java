package one.bartosz.bmonitord.orchestrator.repos;

import one.bartosz.bmonitord.orchestrator.model.target.TargetHTTPInfo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TargetHTTPInfoRepository extends R2dbcRepository<TargetHTTPInfo, UUID> {

    @Query("SELECT * FROM target_http_info WHERE target_id = :targetId and deleted_at is null")
    Mono<TargetHTTPInfo> findByTargetId(UUID targetId);
}
