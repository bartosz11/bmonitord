package one.bartosz.bmonitord.common.repos;

import one.bartosz.bmonitord.common.model.Setting;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends R2dbcRepository<Setting, String> {
}
