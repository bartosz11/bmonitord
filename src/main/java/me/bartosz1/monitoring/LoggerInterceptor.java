package me.bartosz1.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

public class LoggerInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Principal userPrincipal = request.getUserPrincipal();
        String username = null;
        if (userPrincipal != null) username = userPrincipal.getName();
        //example result: [SLF4J stuff] 127.0.0.1 exampleUser -> GET /api/user/current -> 200
        LOGGER.info(getIPAddress(request) + " USER " + username + " -> " + request.getMethod() + " " + request.getServletPath() + " -> " + response.getStatus());
        if (ex != null) ex.printStackTrace();
    }

    private String getIPAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && forwardedFor.length() > 0) return forwardedFor;
        return request.getRemoteAddr();
    }
}
