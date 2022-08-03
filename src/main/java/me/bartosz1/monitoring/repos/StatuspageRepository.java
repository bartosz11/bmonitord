package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.statuspage.Statuspage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatuspageRepository extends JpaRepository<Statuspage, Long> {

    Iterable<Statuspage> findAllByUser(User user);
}
