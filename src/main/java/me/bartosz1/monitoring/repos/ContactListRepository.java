package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactListRepository extends JpaRepository<ContactList, Long> {

    Iterable<ContactList> findAllByUser(User user);
}
