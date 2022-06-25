package me.bartosz1.monitoring.controllers;

import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import me.bartosz1.monitoring.Monitoring;
import me.bartosz1.monitoring.models.Agent;
import me.bartosz1.monitoring.models.Measurement;
import me.bartosz1.monitoring.models.Response;
import me.bartosz1.monitoring.repos.AgentRepository;
import me.bartosz1.monitoring.repos.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;

@org.springframework.web.bind.annotation.RestController
public class IndexController {

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private IncidentRepository incidentRepository;
    private final WriteApi influxWriteApi = Monitoring.getInfluxClient().makeWriteApi(WriteOptions.builder().flushInterval(5000).batchSize(100).build());
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/")
    @ResponseBody
    private ResponseEntity<?> postData(HttpServletRequest req, @RequestBody String body) {
        String[] toParse = body.split("\\|");
        String serverId = toParse[1];
        Optional<Agent> result = agentRepository.findById(serverId);
        if (result.isEmpty()) return new ResponseEntity<>(new Response("agent not found"), HttpStatus.NOT_FOUND);
        Agent agent = result.get();
        agent.setLastDataReceived(Instant.now().getEpochSecond());
        influxWriteApi.writePoint(Measurement.toPoint(toParse));
        //Non visualizable data = data that makes no sense to put in InfluxDB, can't really be used for graphs
        Measurement.parseNonVisualizableData(toParse, agent);
        //IP Address for obvious reasons
        agent.setIpAddress(req.getRemoteAddr());
        agentRepository.save(agent);
        LOGGER.info(req.getRemoteAddr() + " SERVER " + serverId + " -> /");
        //response can be anything, it's ignored anyway
        return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
    }

}
