package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    Iterable<Monitor> findAllByUser(User user);

    //For status checks only, avoids n+1 and cartesian product 😎
    @Query("select distinct m from Monitor m left join fetch m.incidents left join fetch m.agent")
    @QueryHints({@QueryHint(name = "hibernate.query.passDistinctThrough", value = "false")})
    List<Monitor> findAllMonitors();

    @Query("select distinct m from Monitor m left join fetch m.notifications notifications where m in :monitors")
    @QueryHints({@QueryHint(name = "hibernate.query.passDistinctThrough", value = "false")})
    List<Monitor> findAllMonitors(List<Monitor> monitors);

}
