package me.bartosz1.monitoring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.AuthRequest;
import me.bartosz1.monitoring.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(1)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AuthRequest REGISTER_REQUEST = new AuthRequest().setUsername("newaccount").setPassword("#ZbMA4Y32p");

    @Test
    //order's here so this test and the one below won't fail because of wrong execution order
    @Order(1)
    public void registerSucceedsWhenEnabled() throws Exception {
        mockMvc.perform(post("/api/auth/register").content(OBJECT_MAPPER.writeValueAsString(REGISTER_REQUEST)).contentType("application/json"))
                .andExpect(status().is(201)).andDo(print());
        Assertions.assertTrue(userRepository.existsByUsername(REGISTER_REQUEST.getUsername()));
    }

    @Test
    @Order(2)
    public void accountsCantHaveIdenticalUsernames() throws Exception {
        mockMvc.perform(post("/api/auth/register").content(OBJECT_MAPPER.writeValueAsString(REGISTER_REQUEST)).contentType("application/json"))
                .andExpect(status().is(409)).andDo(print());
    }

    @Test
    public void weakPasswordsCantBeUsed() throws Exception {
        //password doesn't match 3 requirements: 8 char length, uppercase letter and a digit
        mockMvc.perform(post("/api/auth/register").content(OBJECT_MAPPER.writeValueAsString(new AuthRequest().setUsername("doesntmatter").setPassword("aaaaa"))).contentType("application/json"))
                .andExpect(status().is(409)).andDo(print());
        Assertions.assertFalse(userRepository.existsByUsername("doesntmatter"));
    }

    @Test
    public void usernameNullCantBeUsed() throws Exception {
        mockMvc.perform(post("/api/auth/register").content(OBJECT_MAPPER.writeValueAsString(new AuthRequest().setUsername("null").setPassword("Something1234"))).contentType("application/json"))
                .andExpect(status().is(400)).andDo(print());
        Assertions.assertFalse(userRepository.existsByUsername("null"));
    }

    @Test
    public void loginFailsOnDisabledAccounts() throws Exception {
        //present in our sample data migration
        AuthRequest disabledAccountLoginRequest = new AuthRequest().setUsername("disabledaccount").setPassword("Test1234");
        mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(disabledAccountLoginRequest)).contentType("application/json"))
                .andExpect(status().is(401)).andDo(print());
        Assertions.assertFalse(userRepository.findByUsername("disabledaccount").get().isEnabled());
    }

    @Test
    public void loginFailsOnInvalidCredentials() throws Exception {
        AuthRequest invalidCredentialsLoginRequest = new AuthRequest().setUsername("testuser").setPassword("thispasswordisinvalid");
        mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(invalidCredentialsLoginRequest)).contentType("application/json"))
                .andExpect(status().is(401)).andDo(print());
    }

    @Test
    public void loginSucceedsOnValidAccount() throws Exception {
        //can be found in R__sample_data.sql
        AuthRequest correctCredentials = new AuthRequest().setUsername("testuser").setPassword("Test1234");
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login").content(OBJECT_MAPPER.writeValueAsString(correctCredentials)).contentType("application/json"))
                .andExpect(status().is(200)).andDo(print()).andReturn().getResponse();
        //validate the token, might be pointless /shrug
        HashMap body = OBJECT_MAPPER.readValue(response.getContentAsString(), HashMap.class);
        String token = (String) body.get("token");
        mockMvc.perform(get("/api/user").header("Authorization", "Bearer " + token))
                .andExpect(status().is(200)).andDo(print());
    }
}
