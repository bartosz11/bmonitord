package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.ContactListCDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.monitor.Monitor;
import me.bartosz1.monitoring.repos.ContactListRepository;
import me.bartosz1.monitoring.repos.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContactListService {

    @Autowired
    private ContactListRepository contactListRepository;
    @Autowired
    private MonitorRepository monitorRepository;

    public ContactList addContactList(User user, ContactListCDO cdo) {
        return contactListRepository.save(new ContactList(cdo, user));
    }

    public ContactList deleteContactList(User user, long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            ContactList contactList = result.get();
            if (contactList.getUser().getId() == user.getId()) {
                Iterable<Monitor> monitors = monitorRepository.findAllByContactList(contactList);
                monitors.forEach(monitor -> monitor.setContactList(null));
                monitorRepository.saveAll(monitors);
                contactListRepository.delete(contactList);
                return contactList;
            }
        }
        return null;
    }

    public ContactList getContactList(User user, long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            ContactList contactList = result.get();
            if (contactList.getUser().getId() == user.getId()) {
                return contactList;
            }
        }
        return null;
    }

    public Iterable<ContactList> listContactLists(User user) {
        return contactListRepository.findAllByUser(user);
    }

    public ContactList modifyContactList(User user, ContactListCDO cdo, long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            if (result.get().getUser().getId() == user.getId()) {
                ContactList contactList = new ContactList(cdo, user).setId(id);
                contactListRepository.save(contactList);
                return contactList;
            }
        }
        return null;
    }
}
