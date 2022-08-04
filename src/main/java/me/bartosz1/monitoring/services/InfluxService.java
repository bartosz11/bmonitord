package me.bartosz1.monitoring.services;

import com.influxdb.client.QueryApi;
import me.bartosz1.monitoring.Monitoring;
import me.bartosz1.monitoring.models.statuspage.StatuspageMonitorObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class InfluxService implements InitializingBean {

    private QueryApi queryApi;
    @Value("${monitoring.influxdb.bucket}")
    private String influxBucket;

    public void getAgentData(String influxMeasurement, String influxQueryDuration, StatuspageMonitorObject statuspageMonitorObject) {
        //todo this might be temporary in both methods
        String influxWindowPeriod = "1m";
        String query = "from(bucket: \"" + influxBucket + "\")  |> range(start: duration(v: " + influxQueryDuration + "))  |> filter(fn: (r) => r[\"_measurement\"] == \"" + influxMeasurement + "\")  |> filter(fn: (r) => r[\"_field\"] == \"ramUsage\" or r[\"_field\"] == \"swapUsage\" or r[\"_field\"] == \"rx\" or r[\"_field\"] == \"tx\" or r[\"_field\"] == \"cpuUsage\" or r[\"_field\"] == \"iowait\" or r[\"_field\"] == \"disksUsagePercent\")  |> aggregateWindow(every: " + influxWindowPeriod + ", fn: mean, createEmpty: false)  |> yield(name: \"mean\")";
        List<Long> timestamps = new ArrayList<>();
        List<Double> cpuUsage = new ArrayList<>();
        List<Double> ramUsage = new ArrayList<>();
        List<Double> iowait = new ArrayList<>();
        List<Long> rx = new ArrayList<>();
        List<Long> tx = new ArrayList<>();
        List<Double> swapUsage = new ArrayList<>();
        List<Double> disksUsage = new ArrayList<>();
        queryApi.query(query).forEach(fluxTable -> {
            fluxTable.getRecords().forEach(r -> {
                Double x = (Double) r.getValue();
                switch (Objects.requireNonNull(r.getField())) {
                    case "cpuUsage" -> {
                        timestamps.add(r.getTime().getEpochSecond());
                        cpuUsage.add(x);
                    }
                    case "ramUsage" -> ramUsage.add(x);
                    case "iowait" -> iowait.add(x);
                    case "swapUsage" -> swapUsage.add(x);
                    case "disksUsagePercent" -> disksUsage.add(x);
                    case "rx" -> rx.add(x.longValue());
                    case "tx" -> tx.add(x.longValue());
                }
            });
        });
        statuspageMonitorObject.setTimestamps(timestamps);
        statuspageMonitorObject.setCpuUsage(cpuUsage);
        statuspageMonitorObject.setRamUsage(ramUsage);
        statuspageMonitorObject.setDisksUsage(disksUsage);
        statuspageMonitorObject.setSwapUsage(swapUsage);
        statuspageMonitorObject.setRx(rx);
        statuspageMonitorObject.setTx(tx);
        statuspageMonitorObject.setIowait(iowait);
    }

    public void getMonitorResponseTime(long influxMeasurement, String influxQueryDuration, StatuspageMonitorObject statuspageMonitorObject) {
        String influxWindowPeriod = "1m";
        String query = "from(bucket: \"" + influxBucket + "\")  |> range(start: duration(v: " + influxQueryDuration + "))  |> filter(fn: (r) => r[\"_measurement\"] == \"" + influxMeasurement + "\")  |> filter(fn: (r) => r[\"_field\"] == \"responseTime\")  |> aggregateWindow(every: " + influxWindowPeriod + ", fn: mean, createEmpty: false)  |> yield(name: \"mean\")";
        List<Long> timestamps = new ArrayList<>();
        List<Long> responseTimes = new ArrayList<>();
        queryApi.query(query).forEach(fluxTable -> {
            fluxTable.getRecords().forEach(fluxRecord -> {
                timestamps.add(fluxRecord.getTime().getEpochSecond());
                responseTimes.add(((Double) fluxRecord.getValue()).longValue());
            });
        });
        statuspageMonitorObject.setTimestamps(timestamps);
        statuspageMonitorObject.setResponseTime(responseTimes);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        queryApi = Monitoring.getInfluxClient().getQueryApi();
    }
}
