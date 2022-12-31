package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.StatuspageAnnouncementCDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.StatuspageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/statuspage")
public class StatuspageController {

    private final StatuspageService statuspageService;

    public StatuspageController(StatuspageService statuspageService) {
        this.statuspageService = statuspageService;
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createStatuspage(@AuthenticationPrincipal User user, @RequestParam String name, @RequestParam(required = false) String monitors) {
        List<Long> ids = new ArrayList<>();
        if (monitors != null) {
            String[] idSplit = monitors.split(",");
            for (String s : idSplit) {
                ids.add(Long.parseLong(s));
            }
        }
        Statuspage statuspage = statuspageService.createStatuspage(user, name, ids);
        return new Response(HttpStatus.CREATED).addAdditionalData(statuspage).toResponseEntity();
    }

    @RequestMapping(path = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteStatuspage(@AuthenticationPrincipal User user, @RequestParam long id) throws EntityNotFoundException {
        statuspageService.deleteStatuspage(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/get", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getStatuspage(@AuthenticationPrincipal User user, @RequestParam long id) throws EntityNotFoundException {
        Statuspage statuspage = statuspageService.getStatuspageByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(statuspage).toResponseEntity();
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listStatuspages(@AuthenticationPrincipal User user) {
        return new Response(HttpStatus.OK).addAdditionalData(statuspageService.getAllStatuspagesByUser(user)).toResponseEntity();
    }

    @RequestMapping(path = "/rename", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> renameStatuspage(@AuthenticationPrincipal User user, long id, String newName) throws EntityNotFoundException {
        Statuspage statuspage = statuspageService.renameStatuspage(user, id, newName);
        return new Response(HttpStatus.OK).addAdditionalField("id", statuspage.getId()).addAdditionalField("name", statuspage.getName()).toResponseEntity();
    }
    //used for editing too
    @RequestMapping(path = "/addannouncement", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> addAnnouncement(@AuthenticationPrincipal User user, @RequestBody StatuspageAnnouncementCDO cdo) throws EntityNotFoundException {
        Statuspage statuspage = statuspageService.addAnnouncement(user, cdo);
        return new Response(HttpStatus.OK).addAdditionalField("statuspageId", statuspage.getId()).addAdditionalField("announcementId", statuspage.getAnnouncement().getId()).toResponseEntity();
    }

    @RequestMapping(path = "/deleteannouncement", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> deleteAnnouncement(@AuthenticationPrincipal User user, @RequestParam(name = "pageid") long statuspageId) throws EntityNotFoundException {
        Statuspage statuspage = statuspageService.removeAnnouncement(user, statuspageId);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}
