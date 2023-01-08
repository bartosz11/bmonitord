package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.repositories.AgentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentRepository agentRepository;

    public AgentController(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> postAgentData(@RequestBody String body) {
        //temporary
        return new Response(HttpStatus.OK).toResponseEntity();
    }
}
