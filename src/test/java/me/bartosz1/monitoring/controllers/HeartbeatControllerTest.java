package me.bartosz1.monitoring.controllers;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.AuthRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(4)
@Transactional
public class HeartbeatControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String authToken;

    @BeforeAll
    public void setup() throws Exception {
        AuthRequest authRequest = new AuthRequest().setUsername("testuser").setPassword("Test1234");
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(authRequest)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        this.authToken = (String) body.get("token");
    }

    @Test
    public void gettingHeartbeatByIdWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{id}", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.responseTime").value(100))
                .andExpect(jsonPath("$.data.timestamp").value(1680534720));
    }

    @Test
    public void gettingHeartbeatByIdWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{id}", 1))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.responseTime").value(100))
                .andExpect(jsonPath("$.data.timestamp").value(1680534720));
    }

    @Test
    public void gettingMonitorsLastHeartbeatWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/last", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.responseTime").value(100))
                .andExpect(jsonPath("$.data.timestamp").value(1680862410));
    }

    @Test
    public void gettingMonitorsLastHeartbeatWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/last", 1))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.responseTime").value(100))
                .andExpect(jsonPath("$.data.timestamp").value(1680862410));
    }

    @Test
    public void gettingHeartbeatPageWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/page", 1).header("Authorization", "Bearer " + authToken)
                        .queryParam("size", "5").queryParam("page", "0").queryParam("sort", "timestamp,desc"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                //if scheduling is disabled, targeted monitor should have only 2 heartbeats created by sample data migration
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    public void gettingHeartbeatPageWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/page", 1)
                        .queryParam("size", "5").queryParam("page", "0").queryParam("sort", "timestamp,desc"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    public void gettingHeartbeatTimerangedPageWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/timerange", 1).header("Authorization", "Bearer " + authToken)
                        .queryParam("size", "5").queryParam("page", "0").queryParam("sort", "timestamp,desc")
                        .queryParam("start", "1680534720"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    public void gettingHeartbeatTimerangedPageWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/heartbeat/{monitorId}/timerange", 1)
                        .queryParam("size", "5").queryParam("page", "0").queryParam("sort", "timestamp,desc")
                        .queryParam("start", "1680534720"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

}
