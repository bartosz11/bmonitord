package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.*;
import me.bartosz1.monitoring.models.PasswordMDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    //Copied from regexr.com/3bfsi
    private final String PASSWORD_VALIDATION_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";
    @Value("${monitoring.registration-enabled}")
    private boolean registrationEnabled;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUserAccount(String username, String password) throws UsernameAlreadyTakenException, InvalidPasswordException, RegistrationDisabledException, IllegalUsernameException {
        if (!registrationEnabled)
            throw new RegistrationDisabledException("Registration of new users is turned off on this instance.");
        //don't ask why lmfao
        if (username.equalsIgnoreCase("null")) throw new IllegalUsernameException("Username null can't be used.");
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException("Username " + username + " is already taken.");
        }
        if (!password.matches(PASSWORD_VALIDATION_PATTERN)) {
            //don't ask why I use error messages like these
            throw new InvalidPasswordException("Invalid password. A valid password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.");
        }
        return userRepository.save(new User().setUsername(username).setPassword(passwordEncoder.encode(password)).setEnabled(true).setLastUpdated(Instant.now().toEpochMilli()));
    }

    //Just so users can delete their own accounts if they want
    public User deleteUserAccount(User user) {
        //no need to mess with auth salts here
        userRepository.delete(user);
        return user;
    }

    public User changeUserPassword(User user, PasswordMDO mdo) throws InvalidOldPasswordException, InvalidPasswordException {
        String oldPassword = mdo.getOldPassword();
        String newPassword = mdo.getNewPassword();
        //Validation of both new and old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new InvalidOldPasswordException("Entered old password is invalid.");
        if (!newPassword.matches(PASSWORD_VALIDATION_PATTERN))
            throw new InvalidPasswordException("Entered new password is invalid. A valid password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.");
        //Change users password and return the new user object
        return userRepository.save(user.setPassword(passwordEncoder.encode(newPassword)).setLastUpdated(Instant.now().toEpochMilli()));
    }

    public User changeUsername(User user, String newUsername) throws UsernameAlreadyTakenException {
        if (userRepository.existsByUsername(newUsername))
            throw new UsernameAlreadyTakenException("Username " + newUsername + " is already taken.");
        return userRepository.save(user.setUsername(newUsername).setLastUpdated(Instant.now().toEpochMilli()));
    }

    public User invalidateAllUserSessions(User user) {
        return userRepository.save(user.setLastUpdated(Instant.now().toEpochMilli()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found."));
    }
}
