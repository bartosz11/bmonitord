package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.*;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.security.JwtTokenUtils;
import me.bartosz1.monitoring.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenUtils jwtTokenUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> login(@RequestBody AuthRequest authRequest) throws InvalidCredentialsException, UserDisabledException {
        //casting go brrrt
        //there's just no way it can return something else than User
        User user = (User) userService.loadUserByUsername(authRequest.getUsername());
        authenticate(authRequest);
        return new Response(HttpStatus.OK).addAdditionalData(new HashMap<String, String>().put("token", jwtTokenUtils.generateToken(user))).toResponseEntity();
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> register(@RequestBody AuthRequest authRequest) throws InvalidPasswordException, UsernameAlreadyTakenException, RegistrationDisabledException, IllegalUsernameException {
        User userAccount = userService.createUserAccount(authRequest.getUsername(), authRequest.getPassword());
        return new Response(HttpStatus.CREATED).addAdditionalData(userAccount).toResponseEntity();
    }

    private void authenticate(AuthRequest authRequest) throws InvalidCredentialsException, UserDisabledException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (DisabledException e) {
            throw new UserDisabledException("User with username " + authRequest.getUsername() + " is disabled.");
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}
