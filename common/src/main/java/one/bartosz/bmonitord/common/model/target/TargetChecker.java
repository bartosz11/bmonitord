package one.bartosz.bmonitord.common.model.target;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("targets_checkers")
//Yet another "helper entity", doesn't need all the dates
public class TargetChecker {

    private UUID id;
    private UUID targetId;
    private UUID checkerId;

    public UUID getId() {
        return id;
    }

    public TargetChecker setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public TargetChecker setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getCheckerId() {
        return checkerId;
    }

    public TargetChecker setCheckerId(UUID checkerId) {
        this.checkerId = checkerId;
        return this;
    }
}
