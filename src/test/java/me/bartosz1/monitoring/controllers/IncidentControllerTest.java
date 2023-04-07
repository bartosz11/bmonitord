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
@Order(5)
@Transactional
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private String authToken;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    public void setup() throws Exception {
        AuthRequest authRequest = new AuthRequest().setUsername("testuser").setPassword("Test1234");
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(authRequest)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        this.authToken = (String) body.get("token");
    }

    @Test
    public void gettingIncidentByIdWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/{id}", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.startTimestamp").value(1680534720));
    }

    @Test
    public void gettingIncidentByIdWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/{id}", 1))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.startTimestamp").value(1680534720));
    }

    @Test
    public void gettingLastIncidentWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/last", 1).queryParam("id", "1").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.startTimestamp").value(1680862410));
    }

    @Test
    public void gettingLastIncidentWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/last").queryParam("id", "1"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.startTimestamp").value(1680862410));
    }

    @Test
    public void gettingIncidentPageWithAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/page").header("Authorization", "Bearer " + authToken)
                        .queryParam("id", "1").queryParam("page", "0").queryParam("size", "5"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    @Test
    public void gettingIncidentPageWithoutAuthTokenSucceeds() throws Exception {
        mockMvc.perform(get("/api/incident/page")
                        .queryParam("id", "1").queryParam("page", "0").queryParam("size", "5"))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }
}
