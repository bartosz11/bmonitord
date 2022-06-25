package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username);
        } else throw new UsernameNotFoundException("User "+username+" not found");
    }

    public User save(AuthRequest authRequest) {
        //First registered user receives admin authority allowing him to do more stuff than usual user
        String authorities;
        if (userRepository.count()==0) {
            authorities = "user,admin";
        } else authorities = "user";
        User user = new User().setEnabled(true).setAuthoritiesAsString(authorities).setUsername(authRequest.getUsername()).setPassword(passwordEncoder.encode(authRequest.getPassword()));
        //not returning the variable here because it doesn't have any ID assigned
        return userRepository.save(user);
    }

}
