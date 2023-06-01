package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    Iterable<Monitor> findAllByUserId(long userId);

    //For status checks only, avoids n+1 and cartesian product 😎
    @Query("select distinct m from Monitor m left join fetch m.incidents left join fetch m.agent left join fetch m.monitorHttpInfo monitorHttpInfo")
    List<Monitor> findAllMonitors();

    @Query("select distinct m from Monitor m left join fetch m.notifications notifications where m in :monitors")
    List<Monitor> findAllMonitors(List<Monitor> monitors);
    @Query("select distinct m from Monitor m left join fetch m.heartbeats heartbeats where m in :monitors")
    List<Monitor> findAllMonitors2(List<Monitor> monitors);

}
