package me.bartosz1.monitoring.controllers;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.WhiteLabelDomain;
import me.bartosz1.monitoring.models.WhiteLabelDomainCDO;
import me.bartosz1.monitoring.repositories.WhiteLabelDomainRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(7)
@Transactional
public class WhiteLabelDomainControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WhiteLabelDomainRepository whiteLabelDomainRepository;
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
    public void domainDeletionSucceeds() throws Exception {
        mockMvc.perform(delete("/api/domain/{id}", 2).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(whiteLabelDomainRepository.existsById(2L));
    }

    @Test
    @Order(2)
    public void domainCreationSucceeds() throws Exception {
        WhiteLabelDomainCDO cdo = new WhiteLabelDomainCDO().setDomain("bmonitord-test3.example.com").setName("Yet another example domain");
        mockMvc.perform(post("/api/domain").header("Authorization", "Bearer " + authToken).contentType("application/json").content(OBJECT_MAPPER.writeValueAsString(cdo)))
                .andExpect(status().is(201)).andDo(print());
        Optional<WhiteLabelDomain> optionalDomain = whiteLabelDomainRepository.findById(3L);
        Assertions.assertTrue(optionalDomain.isPresent());
        WhiteLabelDomain whiteLabelDomain = optionalDomain.get();
        Assertions.assertEquals("Yet another example domain", whiteLabelDomain.getName());
        Assertions.assertEquals("bmonitord-test3.example.com", whiteLabelDomain.getDomain());
        Assertions.assertNull(whiteLabelDomain.getStatuspage());
    }

    @Test
    @Order(3)
    public void getDomainByIdSucceeds() throws Exception {
        mockMvc.perform(get("/api/domain/{id}", 1L).header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data.name").value("Test white label domain"))
                .andExpect(jsonPath("$.data.domain").value("bmonitord-test1.example.com"));
    }

    @Test
    @Order(4)
    public void getAllDomainsSucceeds() throws Exception {
        mockMvc.perform(get("/api/domain").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @Order(5)
    public void modifyDomainSucceeds() throws Exception {
        WhiteLabelDomainCDO cdo = new WhiteLabelDomainCDO().setName("Renamed domain").setDomain("brand-new.domain");
        mockMvc.perform(patch("/api/domain/{id}", 1).header("Authorization", "Bearer " + authToken).contentType("application/json").content(OBJECT_MAPPER.writeValueAsString(cdo)))
                .andExpect(status().is(200)).andDo(print());
        WhiteLabelDomain whiteLabelDomain = whiteLabelDomainRepository.findById(1L).get();
        Assertions.assertEquals("Renamed domain", whiteLabelDomain.getName());
        Assertions.assertEquals("brand-new.domain", whiteLabelDomain.getDomain());
    }
}
