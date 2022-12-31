package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.StatuspageAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatuspageAnnouncementRepository extends JpaRepository<StatuspageAnnouncement, Long> {
}
