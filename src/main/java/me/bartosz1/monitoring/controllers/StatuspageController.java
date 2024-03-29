package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.services.StatuspageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statuspage")
public class StatuspageController {

    private final StatuspageService statuspageService;

    public StatuspageController(StatuspageService statuspageService) {
        this.statuspageService = statuspageService;
    }

    @RequestMapping(path = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createStatuspage(@AuthenticationPrincipal User user, @RequestBody StatuspageCDO cdo) throws IllegalParameterException {
        Statuspage statuspage = statuspageService.createStatuspage(user, cdo);
        return new Response(HttpStatus.CREATED).addAdditionalData(statuspage).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteStatuspage(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        statuspageService.deleteStatuspage(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getStatuspage(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        Statuspage statuspage = statuspageService.getStatuspageByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(statuspage).toResponseEntity();
    }

    @RequestMapping(path = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> listStatuspages(@AuthenticationPrincipal User user) {
        return new Response(HttpStatus.OK).addAdditionalData(statuspageService.getAllStatuspagesByUser(user)).toResponseEntity();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> modifyStatuspage(@AuthenticationPrincipal User user, @PathVariable long id, @RequestBody StatuspageCDO cdo) throws EntityNotFoundException, IllegalParameterException {
        Statuspage statuspage = statuspageService.modifyStatuspage(user, id, cdo);
        return new Response(HttpStatus.OK).addAdditionalData(statuspage).toResponseEntity();
    }

    //used for editing too
    @RequestMapping(path = "/{id}/announcement", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> addAnnouncement(@AuthenticationPrincipal User user, @RequestBody StatuspageAnnouncementCDO cdo, @PathVariable long id) throws EntityNotFoundException {
        StatuspageAnnouncement statuspageAnnouncement = statuspageService.addAnnouncement(user, cdo, id);
        return new Response(HttpStatus.OK).addAdditionalData(statuspageAnnouncement).toResponseEntity();
    }

    @RequestMapping(path = "/{id}/announcement", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> deleteAnnouncement(@AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        statuspageService.removeAnnouncement(user, id);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/{id}/public", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> getStatuspageAsPublicObject(@PathVariable String id) throws EntityNotFoundException {
        return new Response(HttpStatus.OK).addAdditionalData(statuspageService.getStatuspageAsPublicObject(id)).toResponseEntity();
    }

    @RequestMapping(path = "/{statuspageId}/domain/{domainId}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> bindDomainToStatuspage(@PathVariable long statuspageId, @PathVariable long domainId, @AuthenticationPrincipal User user) throws EntityNotFoundException, IllegalParameterException {
        return new Response(HttpStatus.OK).addAdditionalData(statuspageService.bindDomainToStatuspage(statuspageId, domainId, user)).toResponseEntity();
    }

    @RequestMapping(path = "/{statuspageId}/domain", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    private ResponseEntity<Response> unbindDomainToStatuspage(@PathVariable long statuspageId, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        return new Response(HttpStatus.OK).addAdditionalData(statuspageService.unbindDomainToStatuspage(statuspageId, user)).toResponseEntity();
    }
}
