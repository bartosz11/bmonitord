package me.bartosz1.monitoring.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.services.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//Yes, JWT classes are mostly copied from the old version of this project
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtils tokenUtils;
    private final UserService userService;

    public JwtRequestFilter(JwtTokenUtils jwtTokenUtils, UserService userService) {
        this.tokenUtils = jwtTokenUtils;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
            try {
                username = tokenUtils.getUsernameFromToken(token);
            } catch (IllegalArgumentException | JWTVerificationException ignored) {
            }
        }
        if (username != null && username.contains("+") && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = (User) userService.loadUserByUsername(username.split("\\+")[0]);
            if (tokenUtils.validateToken(token, user)) {
                UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                upat.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(upat);
            }
        }
        filterChain.doFilter(request, response);
    }
}
