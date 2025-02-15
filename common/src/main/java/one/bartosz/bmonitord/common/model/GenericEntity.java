package one.bartosz.bmonitord.common.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;
import java.util.UUID;


public abstract class GenericEntity<T extends GenericEntity<T>> {

    @Id
    private UUID id;
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;
    @Column("deleted_at")
    private Instant deletedAt;

    public UUID getId() {
        return id;
    }

    public T setId(UUID id) {
        this.id = id;
        return self();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public T setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return self();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public T setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return self();
    }

    public T softDelete() {
        this.deletedAt = Instant.now();
        return self();
    }

    public T restore() {
        this.deletedAt = null;
        return self();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public T markUpdated() {
        this.updatedAt = Instant.now();
        return self();
    }

    @SuppressWarnings("unchecked")
    // This cast is always safe in this case, since there's no calls on "GenericEntity", only the "child" classes
    //Shortcut, so I don't have to suppress 47234923421 methods
    private T self() {
        return (T) this;
    }
}
