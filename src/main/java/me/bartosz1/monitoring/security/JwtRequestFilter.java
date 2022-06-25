package me.bartosz1.monitoring.security;

import io.jsonwebtoken.ExpiredJwtException;
import me.bartosz1.monitoring.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;
        //the last condition prevents exceptions caused by our custom token filter
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ") && tokenHeader.contains(".")) {
            token = tokenHeader.substring(7);
            try {
                username = tokenUtils.getUsernameFromToken(token);
            } catch (IllegalArgumentException | ExpiredJwtException e) {}
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails details =  userService.loadUserByUsername(username);
            if (tokenUtils.validateToken(token, details)) {
                UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                upat.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(upat);
            }
        }
        filterChain.doFilter(request, response);
    }
}
