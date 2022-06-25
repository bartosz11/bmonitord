package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.Utils;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.repos.AgentRepository;
import me.bartosz1.monitoring.repos.MonitorRepository;
import me.bartosz1.monitoring.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorController.class);
    @Autowired
    private MonitorRepository monitorRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private AgentRepository agentRepository;

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addMonitor(HttpServletRequest req, @RequestBody MonitorCDO monitorCDO, @AuthenticationPrincipal User user) {
        Monitor monitor;
        if (monitorCDO.getType().equalsIgnoreCase("agent")) {
            String id;
            do {
                id = Utils.generateRandomString(16);
            } while (agentRepository.existsById(id));
            Agent agent = new Agent().setId(id);
            monitor = monitorRepo.save(new Monitor(monitorCDO, user, agent));
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/add ID: " + monitor.getId() + " AGENT ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", monitor.getId()).addAdditionalInfo("agentid", id), HttpStatus.CREATED);
        } else monitor = monitorRepo.save(new Monitor(monitorCDO, user));
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/add ID: " + monitor.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", monitor.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteMonitor(HttpServletRequest req, @RequestParam long id, @AuthenticationPrincipal User user) {
        //findByIdAndUser is used to check access
        if (monitorRepo.findByIdAndUser(id, user) != null) {
            monitorRepo.deleteById(id);
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/delete ID: " + id);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/delete ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> listMonitors(HttpServletRequest req, @AuthenticationPrincipal User user) {
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/list");
        Iterable<Monitor> data = monitorRepo.findAllByUserId(user.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalData(data), HttpStatus.OK);
    }

    //just in case I need it later
    @RequestMapping(method = RequestMethod.GET, value = "/get", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getMonitor(HttpServletRequest req, @RequestParam long id, @AuthenticationPrincipal User user) {
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/get ID: " + id);
        Monitor data = monitorRepo.findByIdAndUser(id, user);
        return new ResponseEntity<>(new Response("ok").addAdditionalData(data), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/rename", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> renameMonitor(HttpServletRequest req, @RequestParam long id, @RequestParam String name, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorRepo.findByIdAndUser(id, user);
        if (monitor != null) {
            monitorRepo.save(monitor.setName(name));
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/rename ID: " + id);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.CREATED);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/rename ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/pause", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> pauseMonitor(HttpServletRequest req, @RequestParam long id, @RequestParam boolean pause, @AuthenticationPrincipal User user) {
        Monitor monitor = monitorRepo.findByIdAndUser(id, user);
        if (monitor != null) {
            if (pause) monitor.setLastStatus(MonitorStatus.PAUSED);
            else monitor.setLastStatus(MonitorStatus.UNKNOWN);
            monitorRepo.save(monitor);
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/pause ID: " + id + " PAUSE: " + pause);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.CREATED);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/monitor/pause ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }
}
