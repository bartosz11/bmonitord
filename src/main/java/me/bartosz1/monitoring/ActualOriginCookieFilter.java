package me.bartosz1.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActualOriginCookieFilter extends OncePerRequestFilter {

    private static final String FRONTEND_PATHS_REGEX = "^/(dashboard|auth|statuspage|report)(/.*)?|/index\\.html|/$";
    private final boolean secureCookies;
    private final String origin;

    public ActualOriginCookieFilter(@Value("${monitoring.secure-cookies}") boolean secureCookies, @Value("${monitoring.domain-origin}") String origin) {
        this.secureCookies = secureCookies;
        this.origin = origin;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (servletPath.matches(FRONTEND_PATHS_REGEX)) {
            String setCookie = ResponseCookie.from("actualOrigin", origin).secure(secureCookies).build().toString();
            response.addHeader("Set-Cookie", setCookie);
        }
        filterChain.doFilter(request, response);
    }
}
