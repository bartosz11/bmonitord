package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.HeartbeatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/heartbeat")
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    public HeartbeatController(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> findHeartbeatById(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Heartbeat heartbeat = heartbeatService.findById(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeat).toResponseEntity();
    }

    @RequestMapping(value = "/{monitorId}/last", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> findLastHeartbeatByMonitorId(@PathVariable long monitorId, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        Heartbeat heartbeat = heartbeatService.findLastByMonitorId(monitorId, user);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeat).toResponseEntity();
    }

    @RequestMapping(value = "/{monitorId}/page", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> findHeartbeatPageByMonitorId(@PathVariable long monitorId, @AuthenticationPrincipal User user, @RequestParam int page, @RequestParam int size) throws IllegalParameterException, EntityNotFoundException {
        if (page < 0 || size < 5 || size > 50)
            throw new IllegalParameterException("Page index must be greater than 0. Page size must fit in range 5-50.");
        Page<Heartbeat> heartbeatPage = heartbeatService.findHeartbeatPageByMonitorId(monitorId, PageRequest.of(page, size), user);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeatPage).toResponseEntity();
    }

    @RequestMapping(value = "/{monitorId}/timerange", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> findHeartbeatPageByMonitorIdAndTimerange(@PathVariable long monitorId, @RequestParam int page, @RequestParam int size, @AuthenticationPrincipal User user, @RequestParam long start, @RequestParam(required = false) long end) throws IllegalParameterException, EntityNotFoundException {
        //i hope this makes enough sense
        if (end != 0 && end < start)
            throw new IllegalParameterException("End timestamp cannot be earlier than start timestamp.");
        if (page < 0 || size < 5 || size > 50)
            throw new IllegalParameterException("Page index must be greater than 0. Page size must fit in range 5-50.");
        Page<Heartbeat> heartbeatPage = heartbeatService.findHeartbeatPageByMonitorIdAndTimestampRange(monitorId, PageRequest.of(page, size), user, start, end);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeatPage).toResponseEntity();
    }
}
