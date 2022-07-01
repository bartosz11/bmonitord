package me.bartosz1.monitoring.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenUtils implements InitializingBean {
    //validity = 6 hours in milliseconds
    private static final long VALIDITY = 6 * 60 * 60 * 1000;
    @Value("${monitoring.jwt.secret}")
    private String secret;
    @Value("${monitoring.jwt.issuer}")
    private String issuer;
    private Algorithm algo;
    private JWTVerifier verifier;

    public String generateToken(UserDetails user) {
        return JWT.create()
                .withExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + VALIDITY)).withIssuedAt(Instant.now()).withSubject(user.getUsername()).withIssuer(issuer)
                .sign(algo);
    }

    public boolean validateToken(String token, UserDetails user) {
        DecodedJWT jwt = verifier.verify(token);
        String tokenUsername = jwt.getSubject();
        Date tokenExpiry = jwt.getExpiresAt();
        return (tokenUsername.equals(user.getUsername()) && !tokenExpiry.before(new Date()));
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
