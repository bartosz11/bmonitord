package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.statuspage.Statuspage;
import me.bartosz1.monitoring.models.statuspage.StatuspageAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatuspageAnnouncementRepository extends JpaRepository<StatuspageAnnouncement, Long> {

    void deleteAllByStatuspage(Statuspage statuspage);
}
