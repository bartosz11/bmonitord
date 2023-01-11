package me.bartosz1.monitoring.controllers;

import jakarta.servlet.http.HttpServletRequest;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Agent;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.repositories.AgentRepository;
import me.bartosz1.monitoring.services.HeartbeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentRepository agentRepository;
    private final HeartbeatService heartbeatService;

    public AgentController(AgentRepository agentRepository, HeartbeatService heartbeatService) {
        this.agentRepository = agentRepository;
        this.heartbeatService = heartbeatService;
    }

    @RequestMapping(value = "/{id}/post", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> postAgentData(@RequestBody String body, @PathVariable String id, HttpServletRequest req) throws EntityNotFoundException {
        String[] split = body.split("\\|");
        Optional<Agent> optionalAgent = agentRepository.findById(UUID.fromString(id));
        if (optionalAgent.isPresent()) {
            Agent agent = optionalAgent.get();
            agent.setLastDataReceived(Instant.now().getEpochSecond()).setAgentVersion(split[0]).setOs(new String(Base64.getDecoder().decode(split[1]), StandardCharsets.UTF_8))
                    .setUptime(Integer.parseInt(split[2])).setCpuCores(Integer.parseInt(split[3])).setCpuModel(new String(Base64.getDecoder().decode(split[5]), StandardCharsets.UTF_8))
                    .setIpAddress(getIPAddress(req)).setRamTotal(Long.parseLong(split[7])).setSwapTotal(Long.parseLong(split[9])).setInstalled(true); //set installed just in case
            Heartbeat heartbeat = new Heartbeat().setMonitor(agent.getMonitor()).setCpuFrequency(Integer.parseInt(split[4]))
                    .setCpuUsage(Float.parseFloat(split[6])).setRamUsage(Float.parseFloat(split[8])).setSwapUsage(Float.parseFloat(split[10]))
                    .setIowait(Float.parseFloat(split[11])).setRx(Long.parseLong(split[12])).setTx(Long.parseLong(split[13]))
                    .setDisksUsage(Float.parseFloat(split[15])).setDiskData(new String(Base64.getDecoder().decode(split[14]), StandardCharsets.UTF_8));
            agentRepository.save(agent);
            heartbeatService.saveHeartbeat(heartbeat);
            return new Response(HttpStatus.OK).toResponseEntity();
        }
        throw new EntityNotFoundException("Agent with given ID not found.");
    }

    @RequestMapping(value = "/{id}/install", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> installAgent(@PathVariable String id, HttpServletRequest req) throws EntityNotFoundException {
        Optional<Agent> optionalAgent = agentRepository.findById(UUID.fromString(id));
        if (optionalAgent.isPresent()) {
            Agent agent = optionalAgent.get();
            //this is all we should set
            agent.setInstalled(true).setIpAddress(getIPAddress(req));
            agentRepository.save(agent);
            return new Response(HttpStatus.OK).toResponseEntity();
        }
        throw new EntityNotFoundException("Agent with given ID not found.");
    }


    private String getIPAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && forwardedFor.length() > 0) return forwardedFor;
        return request.getRemoteAddr();
    }
}
