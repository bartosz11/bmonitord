package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.NotificationCDO;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RequestMapping(path = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createNotification(@AuthenticationPrincipal User user, @RequestBody NotificationCDO cdo) {
        Notification notification = notificationService.createNotification(cdo, user);
        return new Response(HttpStatus.CREATED).addAdditionalData(notification).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteNotification(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        notificationService.deleteNotification(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getNotification(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        Notification notification = notificationService.getNotificationByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(notification).toResponseEntity();
    }

    @RequestMapping(path = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listNotifications(@AuthenticationPrincipal User user) {
        return new Response(HttpStatus.OK).addAdditionalData(notificationService.getAllNotificationsByUser(user)).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> modifyNotification(@AuthenticationPrincipal User user, @RequestBody NotificationCDO cdo, @PathVariable long id) throws EntityNotFoundException {
        Notification notification = notificationService.modifyNotification(user, cdo, id);
        return new Response(HttpStatus.OK).addAdditionalData(notification).toResponseEntity();
    }

    @RequestMapping(path = "/{id}/test", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> sendTestNotification(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        notificationService.testNotification(user, id);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}
