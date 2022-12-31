package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.NotificationList;
import me.bartosz1.monitoring.models.NotificationListCDO;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.NotificationListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationListController {

    private final NotificationListService notificationListService;

    public NotificationListController(NotificationListService notificationListService) {
        this.notificationListService = notificationListService;
    }

    @RequestMapping(path = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createNotificationList(@AuthenticationPrincipal User user, @RequestBody NotificationListCDO cdo) {
        NotificationList notificationList = notificationListService.createNotificationList(cdo, user);
        return new Response(HttpStatus.CREATED).addAdditionalData(notificationList).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteNotificationList(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        notificationListService.deleteNotificationList(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getNotificationList(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        NotificationList notificationList = notificationListService.getNotificationListByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(notificationList).toResponseEntity();
    }

    @RequestMapping(path = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listNotificationLists(@AuthenticationPrincipal User user) {
        return new Response(HttpStatus.OK).addAdditionalData(notificationListService.getAllNotificationListsByUser(user)).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> modifyNotificationList(@AuthenticationPrincipal User user, @RequestBody NotificationListCDO cdo, @PathVariable long id) throws EntityNotFoundException {
        NotificationList notificationList = notificationListService.modifyNotificationList(user, cdo, id);
        return new Response(HttpStatus.OK).addAdditionalData(notificationList).toResponseEntity();
    }
}
