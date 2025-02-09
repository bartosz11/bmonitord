package one.bartosz.bmonitord.orchestrator.model;


import org.springframework.data.relational.core.mapping.Table;

@Table("orchestrators")
public class Orchestrator extends GenericEntity<Orchestrator> {

    private String name;
    private String host;
    private boolean leader;

    public String getName() {
        return name;
    }

    public Orchestrator setName(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Orchestrator setHost(String host) {
        this.host = host;
        return this;
    }

    public boolean isLeader() {
        return leader;
    }

    public Orchestrator setLeader(boolean leader) {
        this.leader = leader;
        return this;
    }
}
