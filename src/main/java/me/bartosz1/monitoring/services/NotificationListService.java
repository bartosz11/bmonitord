package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.NotificationList;
import me.bartosz1.monitoring.models.NotificationListCDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.NotificationListRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationListService {

    private final NotificationListRepository notificationListRepository;

    public NotificationListService(NotificationListRepository notificationListRepository) {
        this.notificationListRepository = notificationListRepository;
    }

    public NotificationList createNotificationList(NotificationListCDO cdo, User user) {
        return notificationListRepository.save(new NotificationList(cdo, user));
    }

    public NotificationList deleteNotificationList(long id, User user) throws EntityNotFoundException {
        Optional<NotificationList> byId = notificationListRepository.findById(id);
        if (byId.isPresent()) {
            NotificationList notificationList = byId.get();
            if (notificationList.getUser().getId() == user.getId()) {
                notificationListRepository.delete(notificationList);
                return notificationList;
            }
        }
        throw new EntityNotFoundException("Notification list with ID "+id+" not found.");
    }

    public NotificationList getNotificationListByIdAndUser(long id, User user) throws EntityNotFoundException {
        Optional<NotificationList> byId = notificationListRepository.findById(id);
        if (byId.isPresent()) {
            NotificationList notificationList = byId.get();
            if (notificationList.getUser().getId() == user.getId()) {
                return notificationList;
            }
        }
        throw new EntityNotFoundException("Notification list with ID "+id+" not found.");
    }

    public Iterable<NotificationList> getAllNotificationListsByUser(User user) {
        return notificationListRepository.findAllByUser(user);
    }

    public NotificationList modifyNotificationList(User user, NotificationListCDO cdo, long id) throws EntityNotFoundException {
        Optional<NotificationList> result = notificationListRepository.findById(id);
        if (result.isPresent()) {
            NotificationList notificationList = result.get();
            if (notificationList.getUser().getId() == user.getId()) {
                NotificationList newNotificationList = new NotificationList(cdo, user).setId(id);
                notificationListRepository.save(newNotificationList);
                return newNotificationList;
            }
        }
        throw new EntityNotFoundException("Notification list with ID "+id+" not found.");
    }
}
