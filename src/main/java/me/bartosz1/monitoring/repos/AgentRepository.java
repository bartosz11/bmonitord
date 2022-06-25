package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, String> {

    boolean existsById(String id);
}
