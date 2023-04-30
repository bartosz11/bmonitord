package me.bartosz1.monitoring.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);
    private final String origin;
    private final boolean secureCookies;

    public IndexController(@Value("${monitoring.domain-origin}") String origin, @Value("${monitoring.secure-cookies}") boolean secureCookies) {
        this.origin = origin;
        this.secureCookies = secureCookies;
    }

    @GetMapping("/")
    public String getIndex(HttpServletResponse response) {
        String setCookie = ResponseCookie.from("actualOrigin", origin).secure(secureCookies).build().toString();
        response.addHeader("Set-Cookie", setCookie);
        return "index.html";
    }
}
