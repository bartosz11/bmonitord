package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.NotificationCDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSenderService notificationSenderService;

    public NotificationService(NotificationRepository notificationRepository, NotificationSenderService notificationSenderService) {
        this.notificationRepository = notificationRepository;
        this.notificationSenderService = notificationSenderService;
    }

    public Notification createNotification(NotificationCDO cdo, User user) {
        return notificationRepository.save(new Notification(cdo, user));
    }

    public Notification deleteNotification(long id, User user) throws EntityNotFoundException {
        Optional<Notification> byId = notificationRepository.findById(id);
        if (byId.isPresent()) {
            Notification notification = byId.get();
            if (notification.getUser().getId() == user.getId()) {
                notificationRepository.delete(notification);
                return notification;
            }
        }
        throw new EntityNotFoundException("Notification with ID " + id + " not found.");
    }

    public Notification getNotificationByIdAndUser(long id, User user) throws EntityNotFoundException {
        Optional<Notification> byId = notificationRepository.findById(id);
        if (byId.isPresent()) {
            Notification notification = byId.get();
            if (notification.getUser().getId() == user.getId()) {
                return notification;
            }
        }
        throw new EntityNotFoundException("Notification with ID " + id + " not found.");
    }

    public Iterable<Notification> getAllNotificationsByUser(User user) {
        return notificationRepository.findAllByUser(user);
    }

    public Notification modifyNotification(User user, NotificationCDO cdo, long id) throws EntityNotFoundException {
        Optional<Notification> result = notificationRepository.findById(id);
        if (result.isPresent()) {
            Notification notification = result.get();
            if (notification.getUser().getId() == user.getId()) {
                Notification newNotification = new Notification(cdo, user).setId(id);
                notificationRepository.save(newNotification);
                return newNotification;
            }
        }
        throw new EntityNotFoundException("Notification with ID " + id + " not found.");
    }

    public Notification testNotification(User user, long id) throws EntityNotFoundException {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (notification.getUser().getId() == user.getId()) {
                notificationSenderService.sendTestNotification(notification);
                return notification;
            }
        }
        throw new EntityNotFoundException("Notification with ID " + id + " not found.");
    }
}
