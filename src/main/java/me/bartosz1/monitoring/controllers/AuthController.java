package me.bartosz1.monitoring.controllers;

import jakarta.servlet.http.HttpServletResponse;
import me.bartosz1.monitoring.exceptions.*;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.security.JwtTokenUtils;
import me.bartosz1.monitoring.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    @Value("${monitoring.secure-cookies}")
    private boolean secureCookies;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenUtils jwtTokenUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) throws InvalidCredentialsException, UserDisabledException {
        //casting go brrrt
        //there's just no way it can return something else than User
        User user = (User) userService.loadUserByUsername(authRequest.getUsername());
        authenticate(authRequest);
        String value = jwtTokenUtils.generateToken(user);
        ResponseCookie responseCookie = ResponseCookie.from("auth-token", value).httpOnly(false).sameSite("Strict").secure(secureCookies).path("/").maxAge((int) JwtTokenUtils.VALIDITY/1000).build();
        response.addHeader("Set-Cookie", responseCookie.toString());
        return new Response(HttpStatus.OK).addAdditionalField("token", value).toResponseEntity();
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
