package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.services.MonitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createMonitor(@RequestBody MonitorCDO cdo, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.createMonitor(cdo, user);
        return new Response(HttpStatus.CREATED).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteMonitor(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        monitorService.deleteMonitor(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.GET, value = "", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listMonitors(@AuthenticationPrincipal User user) {
        Iterable<Monitor> data = monitorService.getAllMonitorsByUser(user);
        return new Response(HttpStatus.OK).addAdditionalData(data).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getMonitor(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Monitor monitor = monitorService.getMonitorByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/pause", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> pauseMonitor(@PathVariable long id, @RequestParam boolean pause, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Monitor monitor = monitorService.pauseMonitor(id, pause, user);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/rename", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> renameMonitor(@PathVariable long id, @RequestBody HashMap<String, String> body, @AuthenticationPrincipal User user) throws EntityNotFoundException, IllegalParameterException {
        //subject-to-change I guess
        if (!body.containsKey("name") || body.get("name") == null)
            throw new IllegalParameterException("Name cannot be null.");
        Monitor monitor = monitorService.renameMonitor(id, body.get("name"), user);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{monitorId}/notification/{notificationId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> assignNotificationToMonitor(@AuthenticationPrincipal User user, @PathVariable long monitorId, @PathVariable long notificationId) throws EntityNotFoundException {
        Monitor monitor = monitorService.assignNotficationToMonitor(user, monitorId, notificationId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    //I think using DELETE sort of makes sense here - I'm "deleting" the link between list and monitor
    @RequestMapping(method = RequestMethod.DELETE, value = "/{monitorId}/notification/{notificationId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> unassignNotificationFromMonitor(@AuthenticationPrincipal User user, @PathVariable long monitorId, @PathVariable long notificationId) throws EntityNotFoundException {
        Monitor monitor = monitorService.unassignNotificationFromMonitor(user, monitorId, notificationId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{monitorId}/statuspage/{pageId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> assignStatuspageToMonitor(@AuthenticationPrincipal User user, @PathVariable long monitorId, @PathVariable long pageId) throws EntityNotFoundException {
        Monitor monitor = monitorService.assignMonitorToStatuspage(user, monitorId, pageId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{monitorId}/statuspage/{pageId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> unassignStatuspageFromMonitor(@AuthenticationPrincipal User user, @PathVariable long monitorId, @PathVariable long pageId) throws EntityNotFoundException {
        Monitor monitor = monitorService.unassignMonitorFromStatuspage(user, monitorId, pageId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{monitorId}/publish", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> publishMonitor(@PathVariable long monitorId, @RequestParam(name = "public") boolean published, @AuthenticationPrincipal User user) throws EntityNotFoundException, IllegalParameterException {
        Monitor monitor = monitorService.publishMonitor(monitorId, published, user);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{monitorId}/agent", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getAgentByMonitorId(@PathVariable long monitorId, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Agent agent = monitorService.getAgentByMonitorId(monitorId, user);
        return new Response(HttpStatus.OK).addAdditionalData(agent).toResponseEntity();
    }
    @RequestMapping(method = RequestMethod.PATCH, value = "/{monitorId}/agentIP", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> setAgentIPVisibility(@PathVariable long monitorId, @AuthenticationPrincipal User user, @RequestParam boolean hide) throws EntityNotFoundException {
        Monitor monitor = monitorService.toggleAgentIPVisibility(monitorId, user, hide);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

}

