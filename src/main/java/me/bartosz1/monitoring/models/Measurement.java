package me.bartosz1.monitoring.models;

import com.influxdb.client.write.Point;

import java.util.Base64;

public class Measurement {

    public static Point toPoint(String[] toParse) {
        Point measurement = Point.measurement(toParse[1]);
        measurement.addField("cpuFreq", Integer.parseInt(toParse[5]));
        measurement.addField("cpuUsage", Float.parseFloat(toParse[7]));
        measurement.addField("ramUsage", Float.parseFloat(toParse[9]));
        measurement.addField("swapUsage", Float.parseFloat(toParse[11]));
        measurement.addField("iowait", Float.parseFloat(toParse[12]));
        measurement.addField("rx", Long.parseLong(toParse[13]));
        measurement.addField("tx", Long.parseLong(toParse[14]));
        String[] disks = new String(Base64.getDecoder().decode(toParse[15])).split(";");
        for (int i = 0; i < disks.length; i++) {
            String[] diskData = disks[i].split(",");
            measurement.addField("disk"+i+"Mountpoint", diskData[0]);
            measurement.addField("disk"+i+"UsagePercent", Float.parseFloat(diskData[1]));
            measurement.addField("disk"+i+"TotalBytes", Long.parseLong(diskData[2]));
        }
        return measurement;
    }

    public static void parseNonVisualizableData(String[] toParse, Agent agent) {
        agent.setAgentVersion(toParse[0]);
        agent.setOs(new String(Base64.getDecoder().decode(toParse[2])));
        agent.setUptime(Long.parseLong(toParse[3]));
        agent.setCpuCores(Integer.parseInt(toParse[4]));
        agent.setCpuModel(new String(Base64.getDecoder().decode(toParse[6])));
        agent.setRamTotal(Long.parseLong(toParse[8]));
        agent.setSwapTotal(Long.parseLong(toParse[10]));
    }
}
