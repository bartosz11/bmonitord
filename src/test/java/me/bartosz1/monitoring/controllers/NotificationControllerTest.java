package me.bartosz1.monitoring.controllers;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.NotificationCDO;
import me.bartosz1.monitoring.models.enums.NotificationType;
import me.bartosz1.monitoring.repositories.NotificationRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(3)
@Transactional
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NotificationRepository notificationRepository;

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
    public void creatingNotificationSucceeds() throws Exception {
        NotificationCDO notificationCDO = new NotificationCDO().setName("Email test").setType(NotificationType.EMAIL).setCredentials("email@localhost");
        mockMvc.perform(post("/api/notification").content(OBJECT_MAPPER.writeValueAsString(notificationCDO)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(201)).andDo(print());
        Assertions.assertTrue(notificationRepository.existsById(3L));
    }

    @Test
    @Order(2)
    public void deletingNotificationSucceeds() throws Exception {
        mockMvc.perform(delete("/api/notification/{id}", 2).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(notificationRepository.existsById(2L));
    }

    @Test
    @Order(3)
    public void getNotificationSucceeds() throws Exception {
        mockMvc.perform(get("/api/notification/{id}", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.name").value("Example notification"));
    }

    @Test
    @Order(4)
    public void listNotificationsSucceeds() throws Exception {
        mockMvc.perform(get("/api/notification").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @Order(5)
    public void modifyNotificationSucceeds() throws Exception {
        NotificationCDO notificationCDO = new NotificationCDO().setName("Renamed notification").setType(NotificationType.EMAIL).setCredentials("email@localhost");
        mockMvc.perform(patch("/api/notification/{id}", 1).content(OBJECT_MAPPER.writeValueAsString(notificationCDO)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Notification notification = notificationRepository.findById(1L).get();
        Assertions.assertEquals("Renamed notification", notification.getName());
    }

    @Test
    @Order(6)
    public void sendingTestNotificationSucceeds() throws Exception {
        //subject-to-change - technically it's possible to integration test that with a minimal webserver or greenmail
        mockMvc.perform(post("/api/notification/{id}/test", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
    }
}
