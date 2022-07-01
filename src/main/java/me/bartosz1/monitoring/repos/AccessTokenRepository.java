package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.AccessToken;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    Optional<AccessToken> findByToken(String token);

    Iterable<AccessToken> findAllByUser(User user);
}
