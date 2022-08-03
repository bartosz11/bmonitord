package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.statuspage.Statuspage;
import me.bartosz1.monitoring.models.statuspage.StatuspageAnnouncementCDO;
import me.bartosz1.monitoring.services.InfluxService;
import me.bartosz1.monitoring.services.StatuspageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statuspage")
public class StatuspageController {

    @Autowired
    private StatuspageService statuspageService;
    @Autowired(required = false)
    private InfluxService influxService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatuspageController.class);

    @RequestMapping(path = "/add", method = RequestMethod.POST, produces = "application/json")
    private ResponseEntity<?> createStatuspage(@AuthenticationPrincipal User user, @RequestParam String name, @RequestParam(name = "monitors", required = false) String monitors, HttpServletRequest req) {
        List<Long> ids = new ArrayList<>();
        if (monitors != null) {
            String[] idSplit = monitors.split(",");
            for (int i = 0; i < idSplit.length; i++) {
                ids.add(Long.parseLong(idSplit[i]));
            }
        }
        Statuspage statuspage = statuspageService.createStatuspage(user, name, ids);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/add ID: " + statuspage.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", statuspage.getId()), HttpStatus.OK);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    private ResponseEntity<?> deleteStatuspage(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Statuspage statuspage = statuspageService.deleteStatuspage(id, user);
        if (statuspage != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/delete ID: " + id);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/delete ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(path = "/get", method = RequestMethod.GET, produces = "application/json")
    private ResponseEntity<?> getStatuspage(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Statuspage statuspage = statuspageService.findByIdAndUser(id, user);
        if (statuspage != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/get ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(statuspage), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/get ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET, produces = "application/json")
    private ResponseEntity<?> listStatuspages(@AuthenticationPrincipal User user, HttpServletRequest req) {
        Iterable<Statuspage> data = statuspageService.findAllByUser(user);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/list");
        return new ResponseEntity<>(new Response("ok").addAdditionalData(data), HttpStatus.OK);
    }

    //overrides current announcement too
    @RequestMapping(path = "/addannouncement", method = RequestMethod.POST, produces = "application/json")
    private ResponseEntity<?> addAnnouncement(@AuthenticationPrincipal User user, @RequestBody StatuspageAnnouncementCDO cdo, HttpServletRequest req) {
        Statuspage statuspage = statuspageService.addAnnouncement(user, cdo);
        if (statuspage != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/addannouncement Statuspage ID: " + cdo.getStatuspageId() + " Announcement ID: " + statuspage.getAnnouncement().getId());
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("statuspageId", statuspage.getId()).addAdditionalInfo("announcementId", statuspage.getAnnouncement().getId()), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/addannouncement Statuspage ID: " + cdo.getStatuspageId() + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(path = "/deleteannouncement", method = RequestMethod.DELETE, produces = "application/json")
    private ResponseEntity<?> deleteAnnouncement(@AuthenticationPrincipal User user, @RequestParam(name = "pageid") long statuspageId, HttpServletRequest req) {
        Statuspage statuspage = statuspageService.removeAnnouncement(user, statuspageId);
        if (statuspage != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/deleteannouncement Statuspage ID: " + statuspageId);
            return new ResponseEntity<>(new Response("ok").addAdditionalInfo("statuspageId", statuspageId), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/statuspage/deleteannouncement Statuspage ID: " + statuspageId + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(path = "/stats", method = RequestMethod.GET, produces = "application/json")
    private ResponseEntity<?> getStats(@RequestParam long id, HttpServletRequest req) {
        Statuspage statuspage = statuspageService.getPublicStats(id);
        if (statuspage != null) {
            //needs to be here, otherwise records in statuspages_monitors table are getting removed, probably some hibernate magic
            statuspage.getMonitors().clear();
            LOGGER.info(req.getRemoteAddr()+ " -> /v1/statuspage/stats ID: " +id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(statuspage), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr()+ " -> /v1/statuspage/stats ID: " +id+" FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

}
