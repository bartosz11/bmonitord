package me.bartosz1.monitoring.models;

public enum MonitorStatus {
    //up & down - obvious
    //paused for paused monitors
    //unknown - some kind of placeholder for status after pause to not store two things that could be redundant?
    UP, DOWN, PAUSED, UNKNOWN
}
