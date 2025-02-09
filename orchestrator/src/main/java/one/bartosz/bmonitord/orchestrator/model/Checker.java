package one.bartosz.bmonitord.orchestrator.model;

import one.bartosz.bmonitord.orchestrator.model.target.Target;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("checkers")
public class Checker extends GenericEntity<Checker> {

    private String name;
    private String location;
    private String key;
    @Transient
//    This is kinda the reason I even considered adding soft-deletion and finally added it
    private List<Heartbeat> heartbeats;
    @Transient
    private List<Target> targets;

    public String getName() {
        return name;
    }

    public Checker setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Checker setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getKey() {
        return key;
    }

    public Checker setKey(String key) {
        this.key = key;
        return this;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public Checker setTargets(List<Target> targets) {
        this.targets = targets;
        return this;
    }
}
