package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.IncidentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incident")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getIncidentById(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Incident incident = incidentService.findByIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(incident).toResponseEntity();
    }

    @RequestMapping(value = "/last", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getLastIncidentByMonitorId(@RequestParam long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Incident incident = incidentService.findLastByMonitorIdAndUser(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(incident).toResponseEntity();
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getIncidentPageByMonitorId(@RequestParam long id, @RequestParam int page, @RequestParam int size, @AuthenticationPrincipal User user) throws EntityNotFoundException, IllegalParameterException {
        if (page < 0 || size < 5 || size > 50)
            throw new IllegalParameterException("Page index must be greater than 0. Page size must fit in range 5-50.");
        Page<Incident> incidentPage = incidentService.findIncidentPageByMonitorIdAndUser(id, user, PageRequest.of(page, size));
        return new Response(HttpStatus.OK).addAdditionalData(incidentPage.getContent()).toResponseEntity();
    }
}
