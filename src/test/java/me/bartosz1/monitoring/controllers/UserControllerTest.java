package me.bartosz1.monitoring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.models.PasswordMDO;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//will be always the last one to execute
@Order(20)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    private String authToken;
    private final AuthRequest currentAuthRequest = new AuthRequest().setUsername("testuser").setPassword("Test1234");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    public void setup() throws Exception {
        User user = new User().setEnabled(true).setUsername("testaccount").setPassword("$2a$12$4llvnVh7MsNM2xAjoiSbZ.G.ft5uZCRclLMX62E/ET2V5WLt01sFO").setLastUpdated(Instant.now().getEpochSecond());
        userRepository.save(user);
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(currentAuthRequest)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        this.authToken = (String) body.get("token");
    }


    @Test
    @Order(1)
    public void getAuthenticatedUser() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/user").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        HashMap data = (HashMap) body.get("data");
        Assertions.assertEquals("testuser", data.get("username"));
    }

    @Test
    @Order(2)
    public void changingUsernameToTakenFails() throws Exception {
        mockMvc.perform(patch("/api/user/username").header("Authorization", "Bearer " + authToken).queryParam("username", "disabledaccount"))
                .andExpect(status().is(409)).andDo(print()).andReturn().getResponse();
        Assertions.assertEquals("testuser", userRepository.findById(1L).get().getUsername());
    }

    @Test
    @Order(3)
    public void changingUsernameToNullFails() throws Exception {
        mockMvc.perform(patch("/api/user/username").header("Authorization", "Bearer " + authToken).queryParam("username", "null"))
                .andExpect(status().is(400)).andDo(print()).andReturn().getResponse();
        Assertions.assertEquals("testuser", userRepository.findById(1L).get().getUsername());
    }

    @Test
    @Order(4)
    public void changingUsernameSucceeds() throws Exception {
        mockMvc.perform(patch("/api/user/username").header("Authorization", "Bearer " + this.authToken).queryParam("username", "testuser2"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        Assertions.assertEquals("testuser2", userRepository.findById(1L).get().getUsername());
        this.currentAuthRequest.setUsername("testuser2");
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(currentAuthRequest)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap loginBody = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        this.authToken = (String) loginBody.get("token");
        //required for some reason
        userRepository.save(userRepository.findById(1L).get().setLastUpdated(1680534720));
    }

    @Test
    @Order(5)
    public void changingPasswordFailsWithInvalidOldPassword() throws Exception {
        PasswordMDO mdo = new PasswordMDO().setOldPassword("Test123").setNewPassword("Test12345").setNewPasswordConfirmation("Test12345");
        mockMvc.perform(patch("/api/user/password").header("Authorization", "Bearer " + authToken).content(OBJECT_MAPPER.writeValueAsString(mdo)).contentType("application/json"))
                .andExpect(status().is(409)).andDo(print()).andReturn().getResponse();
    }

    @Test
    @Order(6)
    public void changingPasswordFailsWithNotMatchingNewPasswords() throws Exception {
        PasswordMDO mdo = new PasswordMDO().setOldPassword("Test1234").setNewPassword("Test12345").setNewPasswordConfirmation("Test123456");
        mockMvc.perform(patch("/api/user/password").header("Authorization", "Bearer " + this.authToken).content(OBJECT_MAPPER.writeValueAsString(mdo)).contentType("application/json"))
                .andExpect(status().is(409)).andDo(print()).andReturn().getResponse();
    }

    @Test
    @Order(7)
    public void changingPasswordFailsWithInvalidNewPassword() throws Exception {
        PasswordMDO mdo = new PasswordMDO().setOldPassword("Test1234").setNewPassword("test").setNewPasswordConfirmation("test");
        mockMvc.perform(patch("/api/user/password").header("Authorization", "Bearer " + authToken).content(OBJECT_MAPPER.writeValueAsString(mdo)).contentType("application/json"))
                .andExpect(status().is(409)).andDo(print()).andReturn().getResponse();
    }

    @Test
    @Order(8)
    public void changingPasswordSucceedsWithValidPassword() throws Exception {
        PasswordMDO mdo = new PasswordMDO().setOldPassword("Test1234").setNewPassword("Test12345").setNewPasswordConfirmation("Test12345");
        mockMvc.perform(patch("/api/user/password").header("Authorization", "Bearer " + authToken).content(OBJECT_MAPPER.writeValueAsString(mdo)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        //re-login is left here to check if new credentials are working
        this.currentAuthRequest.setPassword("Test12345");
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(currentAuthRequest)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        this.authToken = (String) body.get("token");
        //required for some reason
        userRepository.save(userRepository.findById(1L).get().setLastUpdated(1680534720));
    }

    @Test
    @Order(9)
    public void invalidatingSessionsSucceeds() throws Exception {
        //invalidate all tokens
        mockMvc.perform(post("/api/user/invalidate").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(200)).andDo(print());
        //check if the same token can be reused again
        mockMvc.perform(get("/api/user").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(403)).andDo(print());
        //required for some reason, re-login isn't required if we reset lastUpdated
        userRepository.save(userRepository.findById(1L).get().setLastUpdated(1680534720));
    }

    @Test
    @Order(10)
    public void deletingAccountWithDataSucceeds() throws Exception {
        mockMvc.perform(delete("/api/user").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(userRepository.existsByUsername("testuser2"));
    }

    //this test can be out of order because it's using a different user account
    @Test
    public void deletingEmptyAccountSucceeds() throws Exception {
        //login
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(new AuthRequest().setUsername("testaccount").setPassword("VgvV&Kt45c"))).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        String authToken = (String) body.get("token");
        //delete the account
        mockMvc.perform(delete("/api/user").header("Authorization", "Bearer " + authToken))
                .andExpect(status().is(204)).andDo(print());
        Assertions.assertFalse(userRepository.existsByUsername("testaccount"));
    }

}
