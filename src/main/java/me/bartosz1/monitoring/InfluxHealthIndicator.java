package me.bartosz1.monitoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

//I know Spring Actuator has one, but I'm using Influx with my own ways, so I have to use this
@Component("influxdb")
public class InfluxHealthIndicator implements HealthIndicator {

    @Value("${monitoring.influxdb.enabled}")
    private boolean influxEnabled;
    @Override
    public Health health() {
        if (influxEnabled) return Health.outOfService().build();
        if (!Monitoring.getInfluxClient().ping()) return Health.down().build();
        return Health.up().build();
    }
}
