package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.monitor.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@org.springframework.stereotype.Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    Iterable<Monitor> findAllByUserId(long userId);

    //SHOULD BE ONLY USED WITH STATUS CHECKS
    @Query("select distinct m from Monitor m left join fetch m.incidents left join fetch m.agent left join fetch m.contactList")
    List<Monitor> findAllMonitors();

    Iterable<Monitor> findAllByContactList(ContactList contactList);

}
