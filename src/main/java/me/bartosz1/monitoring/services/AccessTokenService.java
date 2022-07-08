package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.AccessToken;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repos.AccessTokenRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    public AccessToken addAccessToken(User user, String name) {
        return accessTokenRepository.save(new AccessToken().setToken(RandomStringUtils.random(64, true, true)).setName(name).setUser(user));
    }

    public AccessToken deleteAccessToken(User user, long id) {
        Optional<AccessToken> result = accessTokenRepository.findById(id);
        if (result.isPresent()) {
            AccessToken accessToken = result.get();
            if (accessToken.getUser().getId()==user.getId()) {
                accessTokenRepository.delete(accessToken);
                return accessToken;
            }
        }
        return null;
    }

    public AccessToken findAccessTokenByIdAndUser(User user, long id) {
        Optional<AccessToken> result = accessTokenRepository.findById(id);
        if (result.isPresent()) {
            AccessToken accessToken = result.get();
            if (accessToken.getUser().getId() == user.getId()) {
                return accessToken;
            }
        }
        return null;
    }

    public Iterable<AccessToken> findAllByUser(User user) {
        return accessTokenRepository.findAllByUser(user);
    }
}
