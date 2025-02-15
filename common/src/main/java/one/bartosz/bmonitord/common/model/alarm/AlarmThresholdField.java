package one.bartosz.bmonitord.common.model.alarm;

import one.bartosz.bmonitord.common.model.Heartbeat;

//Fields that can be used in the "THRESHOLD" alarm type
public enum AlarmThresholdField {

    LATENCY("Latency");

    //Used in notifications
    private final String formattedName;

    AlarmThresholdField(String formattedName) {
        this.formattedName = formattedName;
    }

    //Yes, we're casting everything to double since it starts losing precision only after 2^53 and the threshold itself is double already

    public double getValueFromHeartbeat(Heartbeat hb) {
        return switch (this) {
            case LATENCY -> hb.getLatency();
        };
    }
    public String getFormattedName() {
        return formattedName;
    }

}
