package me.bartosz1.monitoring.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import me.bartosz1.monitoring.models.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenUtils implements InitializingBean {
    //subject-to-change - 6 hrs, more might be useful, maybe I should make a parameter on auth to generate a token with requested validity
    public static final long VALIDITY = 6 * 60 * 60 * 1000;
    @Value("${monitoring.jwt.secret}")
    private String secret;
    @Value("${monitoring.jwt.issuer}")
    private String issuer;
    private Algorithm algo;
    private JWTVerifier verifier;

    public String generateToken(User user) {
        return JWT.create()
                .withExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + VALIDITY)).withIssuedAt(Instant.now()).withSubject(user.getUsername()).withIssuer(issuer)
                .sign(algo);
    }

    public boolean validateToken(String token, User user) {
        DecodedJWT jwt = verifier.verify(token);
        String tokenSubject = jwt.getSubject();
        Date tokenExpiry = jwt.getExpiresAt();
        return (tokenSubject.equals(user.getUsername()) && !tokenExpiry.before(new Date()) && jwt.getIssuedAtAsInstant().toEpochMilli() > user.getLastUpdated());
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        algo = Algorithm.HMAC512(secret);
        verifier = JWT.require(algo).withIssuer(issuer).build();
    }
}
