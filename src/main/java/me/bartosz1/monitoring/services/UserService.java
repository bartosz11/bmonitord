package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.*;
import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.PasswordMDO;
import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MonitorRepository monitorRepository;
    private final NotificationRepository notificationRepository;
    private final IncidentRepository incidentRepository;
    private final StatuspageRepository statuspageRepository;
    private final HeartbeatRepository heartbeatRepository;

    //Copied from regexr.com/3bfsi
    private final String PASSWORD_VALIDATION_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";
    @Value("${monitoring.registration-enabled}")
    private boolean registrationEnabled;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MonitorRepository monitorRepository, NotificationRepository notificationRepository, IncidentRepository incidentRepository, StatuspageRepository statuspageRepository, HeartbeatRepository heartbeatRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.monitorRepository = monitorRepository;
        this.notificationRepository = notificationRepository;
        this.incidentRepository = incidentRepository;
        this.statuspageRepository = statuspageRepository;
        this.heartbeatRepository = heartbeatRepository;
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
        //extracted from MonitorService
        //TODO: find a better fix
        userRepository.findById(user.getId()).get().getMonitors().forEach(monitor -> {
            List<Statuspage> bulkSaveStatuspage = new ArrayList<>();
            monitor.getStatuspages().forEach(statuspage -> {
                statuspage.getMonitors().remove(monitor);
                bulkSaveStatuspage.add(statuspage);
            });
            statuspageRepository.saveAll(bulkSaveStatuspage);
            List<Notification> bulkSaveNotification = new ArrayList<>();
            monitor.getNotifications().forEach(notification -> {
                notification.getMonitors().remove(monitor);
                bulkSaveNotification.add(notification);
            });
            notificationRepository.saveAll(bulkSaveNotification);
            incidentRepository.deleteAllByMonitorId(monitor.getId());
            heartbeatRepository.deleteAllByMonitorId(monitor.getId());
            monitorRepository.delete(monitor);
        });
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
