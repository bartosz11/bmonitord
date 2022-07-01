package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.IncidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

//Meant to have only GET endpoints
@RestController
@RequestMapping("/api/v1/incident")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(IncidentController.class);

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getById(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Incident incident = incidentService.getIncidentById(user, id);
        if (incident != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/get ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(incident), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/get ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/last", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getLastByMonitorId(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Incident incident = incidentService.getLastIncidentByMonitorId(user, id);
        if (incident != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/last ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(incident), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/last ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/five", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getLastFiveByMonitorId(@AuthenticationPrincipal User user, @RequestParam long id, @RequestParam int page, HttpServletRequest req) {
        Page<Incident> result = incidentService.getFiveIncidentsByMonitorId(user, id, page);
        if (result != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/five ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(result.toList()), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/incident/five ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }
}
