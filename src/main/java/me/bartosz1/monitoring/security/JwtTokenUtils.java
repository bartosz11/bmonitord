package me.bartosz1.monitoring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtils {
    //validity = 6 hours in milliseconds
    private static final long VALIDITY = 6*60*60*1000;
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(UserDetails user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + VALIDITY))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public boolean validateToken(String token, UserDetails user) {
        Claims claims = getClaimsFromToken(token);
        String tokenUsername = claims.getSubject();
        Date tokenExpiry = claims.getExpiration();
        return (tokenUsername.equals(user.getUsername()) && !tokenExpiry.before(new Date()));
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

}
