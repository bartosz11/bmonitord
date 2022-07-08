package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.monitor.Monitor;
import me.bartosz1.monitoring.models.monitor.MonitorCDO;
import me.bartosz1.monitoring.repos.ContactListRepository;
import me.bartosz1.monitoring.repos.IncidentRepository;
import me.bartosz1.monitoring.services.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@Transactional
@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorController.class);
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private ContactListRepository contactListRepository;

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addMonitor(HttpServletRequest req, @RequestBody MonitorCDO monitorCDO, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.addMonitor(user, monitorCDO);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/add ID: " + monitor.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", monitor.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteMonitor(HttpServletRequest req, @RequestParam long id, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.deleteMonitor(id, user);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/delete ID: " + id);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/delete ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> listMonitors(HttpServletRequest req, @AuthenticationPrincipal User user) {
        Iterable<Monitor> data = monitorService.findAllByUser(user);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/list");
        return new ResponseEntity<>(new Response("ok").addAdditionalData(data), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getMonitor(HttpServletRequest req, @RequestParam long id, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.findByIdAndUser(id, user);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/get ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(monitor), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + "-> /v1/monitor/get ID: " + id);
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/rename", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> renameMonitor(HttpServletRequest req, @RequestParam long id, @RequestParam String name, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.renameMonitor(id, name, user);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/rename ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", monitor.getId()).addAdditionalInfo("name", monitor.getName()), HttpStatus.CREATED);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/rename ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/pause", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> pauseMonitor(HttpServletRequest req, @RequestParam long id, @RequestParam boolean pause, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorService.pauseMonitor(id, pause, user);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/pause ID: " + id + " PAUSE: " + pause);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", monitor.getId()).addAdditionalInfo("pause", monitor.isPaused()), HttpStatus.CREATED);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/pause ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/assignlist", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> assignContactListToMonitor(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId, @RequestParam(name = "listid") long listId) {
        Monitor monitor = monitorService.assignContactList(user, monitorId, listId);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/assignlist Monitor ID: " + monitorId + " List ID: " + listId);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("monitorId", monitor.getId()).addAdditionalInfo("listId", monitor.getContactList().getId()), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/assignlist Monitor ID: " + monitorId + " List ID: " + listId + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/unassignlist", produces = "application/json")
    @ResponseBody
    //There's no need for listId param as monitor can have only one contact list assigned
    public ResponseEntity<?> unassignContactListFromMonitor(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestParam(name = "monitorid") long monitorId) {
        Monitor monitor = monitorService.unassignContactList(user, monitorId);
        if (monitor != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/unassignlist Monitor ID: " + monitorId);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("monitorId", monitor.getId()), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/unassignlist Monitor ID: " + monitorId + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }
}
