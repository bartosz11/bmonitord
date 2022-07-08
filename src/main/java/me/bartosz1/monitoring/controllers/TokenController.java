package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.AccessToken;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.AccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/token")
public class TokenController {
    @Autowired
    private AccessTokenService accessTokenService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenController.class);

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addToken(@AuthenticationPrincipal User user, @RequestParam String name, HttpServletRequest req) {
        AccessToken accessToken = accessTokenService.addAccessToken(user, name);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/add ID: " + accessToken.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", accessToken.getId()).addAdditionalInfo("token", accessToken.getToken()), HttpStatus.OK);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteToken(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        AccessToken accessToken = accessTokenService.deleteAccessToken(user, id);
        if (accessToken != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/delete ID: " + id);
            return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/delete ID: " + id + " FAIL: no access");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getToken(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        AccessToken accessToken = accessTokenService.findAccessTokenByIdAndUser(user, id);
        if (accessToken != null) {
            LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/get ID: " + id);
            return new ResponseEntity<>(new Response("ok").addAdditionalData(accessToken), HttpStatus.OK);
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/get ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> listTokens(@AuthenticationPrincipal User user, HttpServletRequest req) {
        Iterable<AccessToken> accessTokens = accessTokenService.findAllByUser(user);
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/token/list");
        return new ResponseEntity<>(new Response("ok").addAdditionalData(accessTokens), HttpStatus.OK);
    }
}
