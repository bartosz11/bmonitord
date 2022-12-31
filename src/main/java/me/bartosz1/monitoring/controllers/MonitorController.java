package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.MonitorCDO;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.services.MonitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createMonitor(@RequestBody MonitorCDO cdo, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.createMonitor(cdo, user);
        if (monitor.getType() == MonitorType.AGENT)
            return new Response(HttpStatus.CREATED).addAdditionalField("id", monitor.getId()).addAdditionalField("agentid", monitor.getAgent().getId()).toResponseEntity();
        return new Response(HttpStatus.CREATED).addAdditionalField("id", monitor.getId()).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteMonitor(@RequestParam long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        monitorService.deleteMonitor(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listMonitors(@AuthenticationPrincipal User user) {
        Iterable<Monitor> data = monitorService.getAllMonitorsByUser(user);
        return new Response(HttpStatus.OK).addAdditionalData(data).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getMonitor(@RequestParam long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Monitor monitor = monitorService.getMonitorByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/pause", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> pauseMonitor(@RequestParam long id, @RequestParam boolean pause, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Monitor monitor = monitorService.pauseMonitor(id, pause, user);
        return new Response(HttpStatus.OK).addAdditionalField("pause", monitor.isPaused()).addAdditionalField("id", monitor.getId()).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/rename", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> renameMonitor(@RequestParam long id, @RequestParam String name, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Monitor monitor = monitorService.renameMonitor(id, name, user);
        return new Response(HttpStatus.OK).addAdditionalField("name", monitor.getName()).addAdditionalField("id", monitor.getId()).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/assignlist", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> assignNotificationListToMonitor(@AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId, @RequestParam(name = "listid") long listId) throws EntityNotFoundException {
        Monitor monitor = monitorService.assignNotificationListToMonitor(user, monitorId, listId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/unassignlist", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> unassignNotificationListFromMonitor(@AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId) throws EntityNotFoundException {
        Monitor monitor = monitorService.unassignNotificationListFromMonitor(user, monitorId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/assignpage", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> assignStatuspageToMonitor(@AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId, @RequestParam(name = "pageid") long pageId) throws EntityNotFoundException {
        Monitor monitor = monitorService.assignMonitorToStatuspage(user, monitorId, pageId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/unassignpage", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> unassignStatuspageFromMonitor(@AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId, @RequestParam(name = "pageid") long pageId) throws EntityNotFoundException {
        Monitor monitor = monitorService.unassignMonitorFromStatuspage(user, monitorId, pageId);
        return new Response(HttpStatus.OK).addAdditionalData(monitor).toResponseEntity();
    }

}

