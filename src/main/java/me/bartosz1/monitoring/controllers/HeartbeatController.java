package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.HeartbeatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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
    public ResponseEntity<Response> findHeartbeatPageByMonitorId(@PathVariable long monitorId, @AuthenticationPrincipal User user, Pageable pageRequest) throws IllegalParameterException, EntityNotFoundException {
        if (pageRequest.getPageNumber() < 0 || pageRequest.getPageSize() < 5 || pageRequest.getPageSize() > 50)
            throw new IllegalParameterException("Page index must be greater than 0. Page size must fit in range 5-50.");
        Page<Heartbeat> heartbeatPage = heartbeatService.findHeartbeatPageByMonitorId(monitorId, pageRequest, user);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeatPage).toResponseEntity();
    }

    @RequestMapping(value = "/{monitorId}/timerange", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> findHeartbeatPageByMonitorIdAndTimerange(@PathVariable long monitorId, Pageable pageRequest, @AuthenticationPrincipal User user, @RequestParam long start, @RequestParam(required = false) Long end) throws IllegalParameterException, EntityNotFoundException {
        //I hope this makes enough sense
        if (end != null && end != 0 && end < start)
            throw new IllegalParameterException("End timestamp cannot be earlier than start timestamp.");
        if (pageRequest.getPageNumber() < 0 || pageRequest.getPageSize() < 5 || pageRequest.getPageSize() > 50)
            throw new IllegalParameterException("Page index must be greater than 0. Page size must fit in range 5-50.");
        long usingEnd = end == null ? Instant.now().getEpochSecond() : end;
        Page<Heartbeat> heartbeatPage = heartbeatService.findHeartbeatPageByMonitorIdAndTimestampRange(monitorId, pageRequest, user, start, usingEnd);
        return new Response(HttpStatus.OK).addAdditionalData(heartbeatPage).toResponseEntity();
    }
}
