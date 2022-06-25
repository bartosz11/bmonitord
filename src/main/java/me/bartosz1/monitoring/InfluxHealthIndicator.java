package me.bartosz1.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

//I know Spring Actuator has one, but I'm using Influx with my own ways, so I have to use this
@Component("influxdb")
public class InfluxHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        if (!Monitoring.getInfluxClient().ping()) return Health.down().build();
        return Health.up().build();
    }
}
