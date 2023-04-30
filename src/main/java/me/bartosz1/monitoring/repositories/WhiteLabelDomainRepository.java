package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.WhiteLabelDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhiteLabelDomainRepository extends JpaRepository<WhiteLabelDomain, Long> {

    Optional<WhiteLabelDomain> findByDomain(String domain);

    Iterable<WhiteLabelDomain> findAllByUserId(long userId);

    boolean existsById(long id);
}
