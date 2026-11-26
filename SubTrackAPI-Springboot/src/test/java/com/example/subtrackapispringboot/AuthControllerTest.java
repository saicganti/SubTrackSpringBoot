package com.example.subtrackapispringboot;

import com.example.subtrackapispringboot.config.SecurityConfig;
import com.example.subtrackapispringboot.repository.UserRepository;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "controller-test@example.com";
    private static final String TEST_PASSWORD = "TestPass123";

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    private String registerAndGetToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD,
                "firstName", "Controller",
                "lastName", "Test"
        ));

        String responseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseJson).get("token").asText();
    }

    // ---------- register ----------

    @Test
    void register_newEmail_returns200WithToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD,
                "firstName", "Controller",
                "lastName", "Test"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.firstName").value("Controller"))
                .andExpect(jsonPath("$.lastName").value("Test"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        registerAndGetToken();

        String body = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", "AnotherPass123",
                "firstName", "Other",
                "lastName", "User"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        registerAndGetToken();

        String body = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        registerAndGetToken();

        String body = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", "WrongPassword"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "nobody@example.com",
                "password", "SomePass123"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ---------- protected endpoint ----------

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/subscriptions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}