package one.bartosz.bmonitord.orchestrator.config;

import one.bartosz.bmonitord.orchestrator.converters.DurationToIntervalConverter;
import one.bartosz.bmonitord.orchestrator.converters.IntervalToDurationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.List;

@Configuration
public class R2DBCConfig {

    @Bean
    public R2dbcCustomConversions customConversions() {
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, List.of(new DurationToIntervalConverter(), new IntervalToDurationConverter()));
    }
}
