package vn.edu.crs.smartcommunity.sprint3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestStatus;
import vn.edu.crs.smartcommunity.servicerequest.internal.repository.ServiceRequestRepository;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class Sprint3IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdentityLookup identityLookup;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Test
    void residentRequestGetsNormalSlaAndManagerCanReprioritizeOpenWork() throws Exception {
        Long id = createRequest("HIGH");

        mockMvc.perform(get("/api/service-requests/{id}", id).header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("NORMAL"))
                .andExpect(jsonPath("$.dueAt").exists())
                .andExpect(jsonPath("$.overdue").value(false));

        mockMvc.perform(patch("/api/management/service-requests/{id}/priority", id)
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PriorityPayload("HIGH"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void residentCannotChangePriorityAndPriorityCannotChangeAfterWorkStarts() throws Exception {
        Long id = createRequest("NORMAL");

        mockMvc.perform(patch("/api/management/service-requests/{id}/priority", id)
                        .header("Authorization", "Bearer " + residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PriorityPayload("HIGH"))))
                .andExpect(status().isForbidden());

        assign(id);
        mockMvc.perform(patch("/api/technician/service-requests/{id}/start", id)
                        .header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/management/service-requests/{id}/priority", id)
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PriorityPayload("CRITICAL"))))
                .andExpect(status().isConflict());
    }

    @Test
    void overdueIsDerivedAndFinishedRequestsAreNotOverdue() throws Exception {
        Long id = createRequest("NORMAL");
        ServiceRequest request = serviceRequestRepository.findById(id).orElseThrow();
        request.setDueAt(Instant.now().minusSeconds(60));
        serviceRequestRepository.saveAndFlush(request);

        mockMvc.perform(get("/api/service-requests/{id}", id).header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdue").value(true));

        request.setStatus(ServiceRequestStatus.RESOLVED);
        request.setResolvedAt(Instant.now());
        serviceRequestRepository.saveAndFlush(request);
        mockMvc.perform(get("/api/service-requests/{id}", id).header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdue").value(false));
    }

    @Test
    void auditRecordsCreationAndAssignmentAndIsManagementOnly() throws Exception {
        Long id = createRequest("NORMAL");
        String managerToken = managerToken();
        assign(id);

        String logs = mockMvc.perform(get("/api/management/audit-logs")
                        .param("entityType", "SERVICE_REQUEST")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(logs.contains("SERVICE_REQUEST_CREATED"));
        assertTrue(logs.contains("SERVICE_REQUEST_ASSIGNED"));

        mockMvc.perform(get("/api/management/audit-logs").header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardsAreProtectedByRoleAndUseAuthenticatedIdentity() throws Exception {
        mockMvc.perform(get("/api/management/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/management/dashboard").header("Authorization", "Bearer " + managerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceRequests.total").exists())
                .andExpect(jsonPath("$.facilities.total").exists())
                .andExpect(jsonPath("$.recentActivity").isArray());

        mockMvc.perform(get("/api/resident/dashboard").header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resident.fullName").exists())
                .andExpect(jsonPath("$.serviceRequests.open").exists());

        mockMvc.perform(get("/api/technician/dashboard").header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTasks").exists())
                .andExpect(jsonPath("$.recentTasks").isArray());

        mockMvc.perform(get("/api/resident/dashboard").header("Authorization", "Bearer " + managerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void eventAndModuleBoundariesDoNotExposeEntitiesOrInternalRepositories() throws Exception {
        String serviceRequestJava = read("src/main/java/vn/edu/crs/smartcommunity/servicerequest");
        String reportingJava = read("src/main/java/vn/edu/crs/smartcommunity/reporting");
        assertFalse(serviceRequestJava.contains("NotificationRepository"));
        assertFalse(reportingJava.contains("internal.repository"));
    }

    private Long createRequest(String priority) throws Exception {
        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CreatePayload("SLA request " + UUID.randomUUID(),
                                "Please inspect this issue.", "PLUMBING", priority))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void assign(Long id) throws Exception {
        Long technicianId = identityLookup.getUserByEmail("technician@test.com").orElseThrow().userId();
        mockMvc.perform(patch("/api/management/service-requests/{id}/assign", id)
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AssignPayload(technicianId))))
                .andExpect(status().isOk());
    }

    private String residentToken() throws Exception { return loginToken("resident@test.com"); }

    private String managerToken() throws Exception { return loginToken("manager@test.com"); }

    private String technicianToken() throws Exception { return loginToken("technician@test.com"); }

    private String loginToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginPayload(email, "123456"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("accessToken").asText();
    }

    private String json(Object value) throws Exception { return objectMapper.writeValueAsString(value); }

    private String read(String path) throws Exception {
        return java.nio.file.Files.walk(java.nio.file.Path.of(path))
                .filter(file -> file.toString().endsWith(".java"))
                .map(file -> {
                    try { return java.nio.file.Files.readString(file); }
                    catch (Exception exception) { throw new IllegalStateException(exception); }
                }).reduce("", String::concat);
    }

    private record LoginPayload(String email, String password) { }
    private record CreatePayload(String title, String description, String category, String priority) { }
    private record AssignPayload(Long technicianId) { }
    private record PriorityPayload(String priority) { }
}
