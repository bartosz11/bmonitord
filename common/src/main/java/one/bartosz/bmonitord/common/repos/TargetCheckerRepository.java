package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.target.TargetChecker;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TargetCheckerRepository extends R2dbcRepository<TargetChecker, UUID> {

    @Query("select * from targets_checkers where target_id = :targetId")
    Flux<TargetChecker> findAllByTargetId(UUID targetId);

}
