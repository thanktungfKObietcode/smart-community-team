package vn.edu.crs.smartcommunity.sprint5;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class Sprint5HardeningIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void managerCanListOnlyActiveTechniciansWithoutSensitiveFields() throws Exception {
        String body = mockMvc.perform(get("/api/management/technicians")
                        .header("Authorization", "Bearer " + loginToken("manager@test.com")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode technicians = objectMapper.readTree(body);
        assertTrue(technicians.isArray());
        assertTrue(technicians.size() > 0);
        for (JsonNode technician : technicians) {
            assertTrue(technician.get("active").asBoolean());
            assertTrue(technician.get("email").asText().equalsIgnoreCase("technician@test.com"));
            assertTrue(technician.get("password") == null);
        }
    }

    @Test
    void residentCannotAccessTechnicianDirectory() throws Exception {
        mockMvc.perform(get("/api/management/technicians")
                        .header("Authorization", "Bearer " + loginToken("resident@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void serviceRequestGetsUniqueServerGeneratedCode() throws Exception {
        String first = createRequest(null);
        String second = createRequest("CLIENT-SELECTED-CODE");

        String firstCode = objectMapper.readTree(first).get("code").asText();
        String secondCode = objectMapper.readTree(second).get("code").asText();

        assertTrue(firstCode.matches("REQ-[0-9A-F]{8}"));
        assertTrue(secondCode.matches("REQ-[0-9A-F]{8}"));
        assertNotEquals(firstCode, secondCode);
        assertNotEquals("CLIENT-SELECTED-CODE", secondCode);
    }

    @Test
    void serviceRequestResponsesIncludeBusinessCode() throws Exception {
        String response = createRequest(null);
        JsonNode created = objectMapper.readTree(response);

        mockMvc.perform(get("/api/service-requests/{id}", created.get("id").asLong())
                        .header("Authorization", "Bearer " + loginToken("resident@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(created.get("code").asText()));
    }

    private String createRequest(String clientCode) throws Exception {
        String codePart = clientCode == null ? "" : ",\"code\":\"" + clientCode + "\"";
        return mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + loginToken("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hardening request " + UUID.randomUUID()
                                + "\",\"description\":\"Please inspect this issue.\","
                                + "\"category\":\"PLUMBING\",\"priority\":\"NORMAL\"" + codePart + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String loginToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
