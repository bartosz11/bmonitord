package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.WhiteLabelDomain;
import me.bartosz1.monitoring.models.WhiteLabelDomainCDO;
import me.bartosz1.monitoring.services.WhiteLabelDomainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain")
public class WhiteLabelDomainController {

    private final WhiteLabelDomainService whiteLabelDomainService;

    public WhiteLabelDomainController(WhiteLabelDomainService whiteLabelDomainService) {
        this.whiteLabelDomainService = whiteLabelDomainService;
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createDomain(@RequestBody WhiteLabelDomainCDO cdo, @AuthenticationPrincipal User user) {
        WhiteLabelDomain whiteLabelDomain = whiteLabelDomainService.createWhiteLabelDomain(cdo, user);
        return new Response(HttpStatus.CREATED).addAdditionalData(whiteLabelDomain).toResponseEntity();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteDomain(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        whiteLabelDomainService.deleteWhiteLabelDomain(id, user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getDomain(@PathVariable long id, @AuthenticationPrincipal User user) throws EntityNotFoundException {
        WhiteLabelDomain whiteLabelDomainById = whiteLabelDomainService.findWhiteLabelDomainById(id, user);
        return new Response(HttpStatus.OK).addAdditionalData(whiteLabelDomainById).toResponseEntity();
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> getAllDomains(@AuthenticationPrincipal User user) {
        Iterable<WhiteLabelDomain> allWhiteLabelDomains = whiteLabelDomainService.findAllWhiteLabelDomains(user);
        return new Response(HttpStatus.OK).addAdditionalData(allWhiteLabelDomains).toResponseEntity();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> modifyDomain(@RequestBody WhiteLabelDomainCDO cdo, @AuthenticationPrincipal User user, @PathVariable long id) throws EntityNotFoundException {
        WhiteLabelDomain whiteLabelDomain = whiteLabelDomainService.modifyWhiteLabelDomain(id, user, cdo);
        return new Response(HttpStatus.OK).addAdditionalData(whiteLabelDomain).toResponseEntity();
    }
}

