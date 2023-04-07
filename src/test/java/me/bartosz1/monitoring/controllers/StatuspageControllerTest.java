package me.bartosz1.monitoring.controllers;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.StatuspageAnnouncementCDO;
import me.bartosz1.monitoring.models.StatuspageCDO;
import me.bartosz1.monitoring.models.enums.StatuspageAnnouncementType;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
@Order(2)
@Transactional
public class StatuspageControllerTest {

    @Autowired
    private MockMvc mockMvc;
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
    public void gettingStatuspagePublicDataSucceeds() throws Exception {
        mockMvc.perform(get("/api/statuspage/{id}/public", 1))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.name").value("Test statuspage"))
                .andExpect(jsonPath("$.data.logoLink").value("https://google.com/favicon.ico"))
                .andExpect(jsonPath("$.data.logoRedirect").value("https://google.com"))
                .andExpect(jsonPath("$.data.announcement.title").value("Example announcement"))
                .andExpect(jsonPath("$.data.announcement.type").value("WARNING"))
                .andExpect(jsonPath("$.data.announcement.content").value("Example announcement content goes here"))
                .andExpect(jsonPath("$.data.monitors").isArray())
                .andExpect(jsonPath("$.data.monitors", hasSize(1)));
    }

    @Test
    @Order(2)
    public void statuspageCreationSucceeds() throws Exception {
        StatuspageCDO statuspageCDO = new StatuspageCDO().setName("New statuspage").setLogoLink("https://github.com/bartosz11/bmonitord").setLogoRedirect("https://github.com/bartosz11/bmonitord").setMonitorIds(List.of(1L));
        mockMvc.perform(post("/api/statuspage").content(OBJECT_MAPPER.writeValueAsString(statuspageCDO)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(201)).andDo(print());
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(3L);
        Assertions.assertTrue(optionalStatuspage.isPresent());
        Statuspage statuspage = optionalStatuspage.get();
        Assertions.assertEquals(1, statuspage.getMonitors().size());
    }

    @Test
    @Order(3)
    public void statuspageDeletionSucceeds() throws Exception {
        mockMvc.perform(delete("/api/statuspage/{id}", 2).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(statuspageRepository.existsById(2L));
    }

    @Test
    @Order(4)
    public void getStatuspageSucceeds() throws Exception {
        mockMvc.perform(get("/api/statuspage/{id}", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.name").value("Test statuspage"));
    }

    @Test
    @Order(5)
    public void listStatuspagesSucceeds() throws Exception {
        mockMvc.perform(get("/api/statuspage").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @Order(6)
    public void modifyStatuspageSucceeds() throws Exception {
        StatuspageCDO statuspageCDO = new StatuspageCDO().setName("Renamed statuspage");
        mockMvc.perform(patch("/api/statuspage/{id}", 1).content(OBJECT_MAPPER.writeValueAsString(statuspageCDO)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertEquals("Renamed statuspage", statuspage.getName());
    }

    @Test
    @Order(7)
    public void addStatuspageAnnouncementSucceeds() throws Exception {
        StatuspageAnnouncementCDO cdo = new StatuspageAnnouncementCDO().setType(StatuspageAnnouncementType.WARNING).setTitle("Test announcement").setContent("Test announcement content");
        mockMvc.perform(post("/api/statuspage/{id}/announcement", 1).content(OBJECT_MAPPER.writeValueAsString(cdo)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertNotNull(statuspage.getAnnouncement());
        Assertions.assertEquals("Test announcement", statuspage.getAnnouncement().getTitle());
    }

    @Test
    @Order(8)
    public void modifyStatuspageAnnouncementSucceeds() throws Exception {
        StatuspageAnnouncementCDO cdo = new StatuspageAnnouncementCDO().setType(StatuspageAnnouncementType.WARNING).setTitle("Renamed announcement").setContent("Renamed announcement content");
        mockMvc.perform(post("/api/statuspage/{id}/announcement", 1).content(OBJECT_MAPPER.writeValueAsString(cdo)).contentType("application/json").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertNotNull(statuspage.getAnnouncement());
        Assertions.assertEquals("Renamed announcement", statuspage.getAnnouncement().getTitle());
    }

    @Test
    @Order(9)
    public void deleteStatuspageAnnouncementSucceeds() throws Exception {
        mockMvc.perform(delete("/api/statuspage/{id}/announcement", 1).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Statuspage statuspage = statuspageRepository.findById(1L).get();
        Assertions.assertNull(statuspage.getAnnouncement());
    }

}
