package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.Utils;
import me.bartosz1.monitoring.models.AccessToken;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repos.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/token")
public class TokenController {
    //todo logging
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addToken(@AuthenticationPrincipal User user, @RequestParam String name, HttpServletRequest req) {
        AccessToken accessToken = accessTokenRepository.save(new AccessToken().setToken(Utils.generateRandomString(64)).setUser(user).setName(name));
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", accessToken.getId()).addAdditionalInfo("token", accessToken.getToken()), HttpStatus.OK);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteToken(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Optional<AccessToken> result = accessTokenRepository.findById(id);
        if (result.isEmpty()) return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
        AccessToken accessToken = result.get();
        if (accessToken.getUser().getId() != user.getId()) return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
        accessTokenRepository.delete(accessToken);
        return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getToken(@AuthenticationPrincipal User user, @RequestParam long id, HttpServletRequest req) {
        Optional<AccessToken> result = accessTokenRepository.findById(id);
        if (result.isEmpty()) return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
        AccessToken accessToken = result.get();
        if (accessToken.getUser().getId() != user.getId()) return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(new Response("ok").addAdditionalData(accessToken), HttpStatus.OK);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> listTokens(@AuthenticationPrincipal User user, HttpServletRequest req) {
        Iterable<AccessToken> accessTokens = accessTokenRepository.findAllByUser(user);
        return new ResponseEntity<>(new Response("ok").addAdditionalData(accessTokens), HttpStatus.OK);
    }
}
