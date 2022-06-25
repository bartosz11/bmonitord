package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.security.JwtTokenUtils;
import me.bartosz1.monitoring.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v1/auth")
//There's no logout endpoint as JWT tokens can't really be invalidated
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtils tokenUtils;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest req) throws Exception {
        authenticate(request.getUsername(), request.getPassword(), req.getRemoteAddr());
        UserDetails user = userService.loadUserByUsername(request.getUsername());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("token", tokenUtils.generateToken(user)), HttpStatus.OK);
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody AuthRequest request, HttpServletRequest req) {
        User user = userService.save(request);
        LOGGER.info(req.getRemoteAddr()+" -> USER "+user.getUsername()+" REGISTER SUCCESS");
        return new ResponseEntity<>(
                new Response("ok")
                        .addAdditionalInfo("username", user.getUsername())
                        .addAdditionalInfo("authorities", user.getAuthoritiesAsString())
                        .addAdditionalInfo("id", user.getId()),
                HttpStatus.OK
        );
    }

    @RequestMapping(path = "/current", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal User user, HttpServletRequest req) {
        LOGGER.info(req.getRemoteAddr()+" USER "+user.getId()+" -> /v1/auth/currentuser");
        return new ResponseEntity<>(
                new Response("ok")
                        .addAdditionalInfo("username", user.getUsername())
                        .addAdditionalInfo("authorities", user.getAuthoritiesAsString())
                        .addAdditionalInfo("id", user.getId()),
                HttpStatus.OK
        );
    }

    private void authenticate(String username, String password, String addr) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            LOGGER.info(addr+" -> USER "+username+" AUTH SUCCESS");
        } catch (DisabledException e) {
            LOGGER.info(addr+" -> USER "+username+" AUTH FAIL: ACCOUNT DISABLED");
            throw new DisabledException("User " + username + " disabled", e);
        } catch (BadCredentialsException e) {
            LOGGER.info(addr+" -> USER "+username+" AUTH FAIL: INVALID CREDENTIALS");
            throw new BadCredentialsException("Invalid credentials", e);
        }
    }
}
