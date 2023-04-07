package me.bartosz1.monitoring.controllers;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.NotificationRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(6)
//tests gotta rollback their wrong-doings, especially on a test-only db? lmfao
@Transactional
//ofc I'm not testing all EntityNotFoundException cases, I cba doing that
public class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MonitorRepository monitorRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StatuspageRepository statuspageRepository;

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
    @Order(1)
    public void monitorCreationSucceeds() throws Exception {
        MonitorCDO monitorCDO = new MonitorCDO().setName("Example").setHost("http://example.com").setTimeout(5).setRetries(0).setVerifyCertificate(false).setPublished(false).setAllowedHttpCodes("200");
        mockMvc.perform(post("/api/monitor").header("Authorization", "Bearer " + authToken).content(OBJECT_MAPPER.writeValueAsString(monitorCDO)).contentType("application/json"))
                .andExpect(status().is(201)).andDo(print());
        //should be id 3
        Assertions.assertTrue(monitorRepository.existsById(3L));
    }

    @Test
    @Order(2)
    public void getMonitorSucceeds() throws Exception {
        mockMvc.perform(get("/api/monitor/{id}", 1).header("Authorization", "Bearer " + authToken))
                //I guess comparing name is good enough, why would I compare IDs in this case
                .andExpect(status().is(200)).andExpect(jsonPath("$.data.name").value("Google")).andDo(print());
    }

    @Test
    @Order(3)
    public void listMonitorsSucceeds() throws Exception {
        mockMvc.perform(get("/api/monitor").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @Order(4)
    public void pauseMonitorSucceeds() throws Exception {
        mockMvc.perform(patch("/api/monitor/{id}/pause", 1).queryParam("pause", "true").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Assertions.assertTrue(monitor.isPaused());
    }

    @Test
    @Order(5)
    public void renameMonitorSucceeds() throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "Renamed Google");
        mockMvc.perform(patch("/api/monitor/{id}/rename", 1).contentType("application/json").content(OBJECT_MAPPER.writeValueAsString(hashMap)).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Assertions.assertEquals("Renamed Google", monitor.getName());
    }

    @Test
    @Order(6)
    public void publishMonitorSucceeds() throws Exception {
        mockMvc.perform(patch("/api/monitor/{id}/publish", 1).queryParam("public", "true").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Assertions.assertTrue(monitor.isPublished());
    }

    @Test
    @Order(7)
    public void unbindNotificationFromMonitorSucceeds() throws Exception {
        mockMvc.perform(delete("/api/monitor/{monitorId}/notification/{notificationId}", 1, 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Notification notification = notificationRepository.findById(1L).get();
        Assertions.assertFalse(monitor.getNotifications().contains(notification));
        Assertions.assertFalse(notification.getMonitors().contains(monitor));
    }

    @Test
    @Order(8)
    public void bindNotificationToMonitorSucceeds() throws Exception {
        mockMvc.perform(patch("/api/monitor/{monitorId}/notification/{notificationId}", 1, 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Notification notification = notificationRepository.findById(1L).get();
        Assertions.assertTrue(monitor.getNotifications().contains(notification));
        Assertions.assertTrue(notification.getMonitors().contains(monitor));
    }

    @Test
    @Order(9)
    public void unbindStatuspageFromMonitorSucceeds() throws Exception {
        mockMvc.perform(delete("/api/monitor/{monitorId}/statuspage/{statuspageId}", 1, 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertFalse(monitor.getStatuspages().contains(statuspage));
        Assertions.assertFalse(statuspage.getMonitors().contains(monitor));
    }

    @Test
    @Order(10)
    public void bindStatuspageToMonitorSucceeds() throws Exception {
        mockMvc.perform(patch("/api/monitor/{monitorId}/statuspage/{statuspageId}", 1, 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Monitor monitor = monitorRepository.findById(1L).get();
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertTrue(monitor.getStatuspages().contains(statuspage));
        Assertions.assertTrue(statuspage.getMonitors().contains(monitor));
    }

    @Test
    @Order(11)
    public void monitorDeletionSucceeds() throws Exception {
        mockMvc.perform(delete("/api/monitor/{id}", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(monitorRepository.existsById(1L));
    }
}
