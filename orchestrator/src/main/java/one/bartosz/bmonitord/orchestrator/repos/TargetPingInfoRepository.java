package one.bartosz.bmonitord.orchestrator.repos;

import one.bartosz.bmonitord.orchestrator.model.target.TargetPingInfo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TargetPingInfoRepository extends R2dbcRepository<TargetPingInfo, UUID> {

    @Query("select * from target_ping_info where target_id = :targetId and deleted_at is null")
    Mono<TargetPingInfo> findByTargetId(UUID targetId);
}
