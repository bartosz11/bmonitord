package me.bartosz1.monitoring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/dashboard/**", "/auth/**", "/report/**", "/statuspage/**", "/"})
    public String getIndex() {
        return "/index.html";
    }
}
