package vn.edu.crs.smartcommunity.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import vn.edu.crs.smartcommunity.identity.internal.config.DemoDataInitializer;
import vn.edu.crs.smartcommunity.identity.internal.entity.Role;
import vn.edu.crs.smartcommunity.identity.internal.entity.RoleName;
import vn.edu.crs.smartcommunity.identity.internal.entity.User;
import vn.edu.crs.smartcommunity.identity.internal.repository.RoleRepository;
import vn.edu.crs.smartcommunity.identity.internal.repository.UserRepository;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class IdentityAuthenticationIntegrationTests {

    private static final String RESIDENT_EMAIL = "resident@test.com";
    private static final String DEVELOPMENT_PASSWORD = "123456";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private DemoDataInitializer demoDataInitializer;

    @BeforeEach
    void ensureInactiveTestUser() {
        Role residentRole = roleRepository.findByName(RoleName.RESIDENT).orElseThrow();
        User inactiveUser = userRepository.findByEmailIgnoreCase("inactive@test.com")
                .orElseGet(User::new);
        inactiveUser.setFullName("Inactive Demo");
        inactiveUser.setEmail("inactive@test.com");
        inactiveUser.setPassword(passwordEncoder.encode(DEVELOPMENT_PASSWORD));
        inactiveUser.setActive(false);
        inactiveUser.getRoles().add(residentRole);
        userRepository.save(inactiveUser);
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Smart Community"));
    }

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        mockMvc.perform(loginRequest(RESIDENT_EMAIL, DEVELOPMENT_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200))
                .andExpect(jsonPath("$.fullName").value("Cư dân Demo"))
                .andExpect(jsonPath("$.email").value(RESIDENT_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("RESIDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void wrongPasswordReturnsUnauthorizedWithGenericMessage() throws Exception {
        mockMvc.perform(loginRequest(RESIDENT_EMAIL, "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    void unknownEmailReturnsUnauthorizedWithSameGenericMessage() throws Exception {
        mockMvc.perform(loginRequest("unknown@test.com", DEVELOPMENT_PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    void inactiveAccountReturnsForbidden() throws Exception {
        mockMvc.perform(loginRequest("inactive@test.com", DEVELOPMENT_PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is inactive"));
    }

    @Test
    void protectedEndpointWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserUsesAuthenticatedJwtClaims() throws Exception {
        String token = loginToken(RESIDENT_EMAIL, DEVELOPMENT_PASSWORD);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.fullName").value("Cư dân Demo"))
                .andExpect(jsonPath("$.email").value(RESIDENT_EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("RESIDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void jwtContainsExpectedRolesAndMapsThemToRoleAuthority() throws Exception {
        String token = loginToken(RESIDENT_EMAIL, DEVELOPMENT_PASSWORD);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals(RESIDENT_EMAIL, jwt.getSubject());
        assertEquals("Cư dân Demo", jwt.getClaimAsString("fullName"));
        assertEquals(List.of("RESIDENT"), jwt.getClaimAsStringList("roles"));
        assertNotNull(jwt.getClaim("userId"));
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertTrue(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).equals(Duration.ofHours(2)));

        AbstractAuthenticationToken authentication = jwtAuthenticationConverter.convert(jwt);
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_RESIDENT"::equals));
    }

    @Test
    void storedDemoPasswordsAreBcryptHashed() {
        List<String> demoEmails = List.of(
                "admin@test.com",
                "manager@test.com",
                "resident@test.com",
                "technician@test.com",
                "security@test.com");

        demoEmails.forEach(email -> {
            User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
            assertFalse(user.getPassword().equals(DEVELOPMENT_PASSWORD));
            assertTrue(user.getPassword().startsWith("$2"));
            assertTrue(passwordEncoder.matches(DEVELOPMENT_PASSWORD, user.getPassword()));
        });
    }

    @Test
    void repeatedInitializerExecutionDoesNotCreateDuplicateDemoUsers() throws Exception {
        demoDataInitializer.run();
        demoDataInitializer.run();

        List<String> demoEmails = List.of(
                "admin@test.com",
                "manager@test.com",
                RESIDENT_EMAIL,
                "technician@test.com",
                "security@test.com");

        for (String email : demoEmails) {
            long matchingUsers = userRepository.findAll().stream()
                    .filter(user -> user.getEmail().equalsIgnoreCase(email))
                    .count();
            assertEquals(1, matchingUsers, "duplicate user for " + email);
        }
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
            String email,
            String password) throws Exception {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginPayload(email, password)));
    }

    private String loginToken(String email, String password) throws Exception {
        String response = mockMvc.perform(loginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private record LoginPayload(String email, String password) {
    }
}
