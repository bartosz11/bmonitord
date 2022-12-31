package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.exceptions.InvalidOldPasswordException;
import me.bartosz1.monitoring.exceptions.InvalidPasswordException;
import me.bartosz1.monitoring.exceptions.UsernameAlreadyTakenException;
import me.bartosz1.monitoring.models.PasswordMDO;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/current", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> currentUser(@AuthenticationPrincipal User user) {
        return new Response(HttpStatus.OK).addAdditionalData(user).toResponseEntity();
    }

    @RequestMapping(path = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> deleteUserAccount(@AuthenticationPrincipal User user) {
        userService.deleteUserAccount(user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @RequestMapping(path = "/username", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> changeUsername(@AuthenticationPrincipal User user, @RequestParam(name = "username") String newUsername) throws UsernameAlreadyTakenException {
        User user1 = userService.changeUsername(user, newUsername);
        return new Response(HttpStatus.OK).addAdditionalData(user1).toResponseEntity();
    }

    @RequestMapping(path = "/password", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> changePassword(@AuthenticationPrincipal User user, @RequestBody PasswordMDO mdo) throws InvalidPasswordException, InvalidOldPasswordException {
        if (mdo.getNewPassword().equals(mdo.getNewPasswordConfirmation())) {
            userService.changeUserPassword(user, mdo);
            return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
        }
        //I hope this is understandable
        throw new InvalidPasswordException("New passwords don't match.");
    }

    @RequestMapping(path = "/invalidate", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> invalidateAllSessions(@AuthenticationPrincipal User user) {
        userService.invalidateAllUserSessions(user);
        return new Response(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}
