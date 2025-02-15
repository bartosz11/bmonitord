package one.bartosz.bmonitord.common.model.alarm;

import one.bartosz.bmonitord.common.model.GenericEntity;
import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.Notification;
import one.bartosz.bmonitord.common.model.target.Target;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("alarms")
//Also known as "triggers" here and there
public class Alarm extends GenericEntity<Alarm> {

    private String name;
    private AlarmType type;
    //If the alarm "went off" or not
    private boolean active;
    //To disable this alarm temporarily
    private boolean muted;

    //Probably the most "universal" type for thresholds
    private double threshold;
    private AlarmThresholdField thresholdField;
    private UUID targetId;
    @Transient
    private Target target;
    @Transient
    private List<Notification> notifications;

    public String getName() {
        return name;
    }

    public Alarm setName(String name) {
        this.name = name;
        return this;
    }

    public AlarmType getType() {
        return type;
    }

    public Alarm setType(AlarmType alarmType) {
        this.type = alarmType;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Alarm setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isMuted() {
        return muted;
    }

    public Alarm setMuted(boolean muted) {
        this.muted = muted;
        return this;
    }

    public double getThreshold() {
        return threshold;
    }

    public Alarm setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Alarm setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Alarm setTarget(Target target) {
        this.target = target;
        return this;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public Alarm setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }

    public AlarmThresholdField getThresholdField() {
        return thresholdField;
    }

    public Alarm setThresholdField(AlarmThresholdField thresholdField) {
        this.thresholdField = thresholdField;
        return this;
    }

}
