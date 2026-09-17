package vn.edu.crs.smartcommunity.phase3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.notification.internal.entity.Notification;
import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;
import vn.edu.crs.smartcommunity.notification.internal.repository.NotificationRepository;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestStatus;
import vn.edu.crs.smartcommunity.servicerequest.internal.repository.ServiceRequestRepository;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class ServiceRequestNotificationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdentityLookup identityLookup;

    @Autowired
    private ResidentLookup residentLookup;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void completeResidentManagerTechnicianResidentWorkflowWorks() throws Exception {
        Flow flow = createRequest();

        assertStatus(flow.requestId(), ServiceRequestStatus.OPEN);

        mockMvc.perform(patch("/api/management/service-requests/{id}/assign", flow.requestId())
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AssignPayload(flow.technicianUserId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTechnicianId").value(flow.technicianUserId()));

        Notification assignmentNotification = findNotification(
                flow.technicianUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_ASSIGNED);
        assertNotNull(assignmentNotification);

        mockMvc.perform(patch("/api/technician/service-requests/{id}/start", flow.requestId())
                        .header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/technician/service-requests/{id}/resolve", flow.requestId())
                        .header("Authorization", "Bearer " + technicianToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ResolvePayload("Repaired the water pump."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Repaired the water pump."));

        Notification resolvedNotification = findNotification(
                flow.residentUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_RESOLVED);
        assertNotNull(resolvedNotification);

        mockMvc.perform(patch("/api/service-requests/{id}/confirm", flow.requestId())
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        assertStatus(flow.requestId(), ServiceRequestStatus.CLOSED);
    }

    @Test
    void assignmentCreatesNotificationForTechnician() throws Exception {
        Flow flow = createRequest();
        assign(flow);

        mockMvc.perform(get("/api/notifications/my")
                        .header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + findNotification(
                        flow.technicianUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_ASSIGNED).getId()
                        + ")]" ).exists());
    }

    @Test
    void resolveCreatesNotificationForResident() throws Exception {
        Flow flow = createRequest();
        assign(flow);
        start(flow);
        resolve(flow);

        mockMvc.perform(get("/api/notifications/my")
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + findNotification(
                        flow.residentUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_RESOLVED).getId()
                        + ")]" ).exists());
    }

    @Test
    void notificationOwnerCanListAndMarkNotificationRead() throws Exception {
        Flow flow = createRequest();
        assign(flow);
        Notification notification = findNotification(
                flow.technicianUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_ASSIGNED);

        mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void anotherUserCannotReadOrModifySomeoneElsesNotification() throws Exception {
        Flow flow = createRequest();
        assign(flow);
        Notification notification = findNotification(
                flow.technicianUserId(), flow.requestId(), NotificationType.SERVICE_REQUEST_ASSIGNED);

        mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isNotFound());

        String residentNotifications = mockMvc.perform(get("/api/notifications/my")
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertFalse(residentNotifications.contains("SERVICE_REQUEST_ASSIGNED"));
    }

    @Test
    void residentCannotAssignOrTechnicianCannotResolveAnotherTechniciansRequest() throws Exception {
        Flow flow = createRequest();

        mockMvc.perform(patch("/api/management/service-requests/{id}/assign", flow.requestId())
                        .header("Authorization", "Bearer " + residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AssignPayload(flow.technicianUserId()))))
                .andExpect(status().isForbidden());

        assign(flow);
        Long otherTechnician = identityLookup.getUserByEmail("security@test.com")
                .orElseThrow()
                .userId();
        String otherToken = loginToken("security@test.com", "123456");

        mockMvc.perform(patch("/api/technician/service-requests/{id}/start", flow.requestId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        assertTrue(otherTechnician > 0);
    }

    @Test
    void serviceRequestEventsDoNotExposeJPAEntitiesOrNotificationRepository() throws Exception {
        String serviceRequestSource = readSource("servicerequest");
        String eventSource = readSource("servicerequest", "api");
        assertFalse(serviceRequestSource.contains("NotificationRepository"));
        assertFalse(eventSource.contains("internal.entity.ServiceRequest"));
        assertFalse(eventSource.contains("Notification"));
    }

    private Flow createRequest() throws Exception {
        Long residentUserId = identityLookup.getUserByEmail("resident@test.com")
                .orElseThrow()
                .userId();
        Long residentId = residentLookup.getByUserId(residentUserId).orElseThrow().residentId();
        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CreatePayload(
                                "Water leak " + UUID.randomUUID(),
                                "There is a leak near the kitchen sink.",
                                "PLUMBING",
                                "HIGH"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.residentId").value(residentId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new Flow(json.get("id").asLong(), residentUserId, technicianUserId());
    }

    private void assign(Flow flow) throws Exception {
        mockMvc.perform(patch("/api/management/service-requests/{id}/assign", flow.requestId())
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AssignPayload(flow.technicianUserId()))))
                .andExpect(status().isOk());
    }

    private void start(Flow flow) throws Exception {
        mockMvc.perform(patch("/api/technician/service-requests/{id}/start", flow.requestId())
                        .header("Authorization", "Bearer " + technicianToken()))
                .andExpect(status().isOk());
    }

    private void resolve(Flow flow) throws Exception {
        mockMvc.perform(patch("/api/technician/service-requests/{id}/resolve", flow.requestId())
                        .header("Authorization", "Bearer " + technicianToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ResolvePayload("Repaired the water pump."))))
                .andExpect(status().isOk());
    }

    private Notification findNotification(Long userId, Long requestId, NotificationType type) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(notification -> notification.getType() == type)
                .filter(notification -> notification.getMessage().contains("#" + requestId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Notification not found for request " + requestId));
    }

    private void assertStatus(Long requestId, ServiceRequestStatus expected) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(expected, serviceRequest.getStatus());
    }

    private Long technicianUserId() {
        return identityLookup.getUserByEmail("technician@test.com").orElseThrow().userId();
    }

    private String residentToken() throws Exception {
        return loginToken("resident@test.com", "123456");
    }

    private String managerToken() throws Exception {
        return loginToken("manager@test.com", "123456");
    }

    private String technicianToken() throws Exception {
        return loginToken("technician@test.com", "123456");
    }

    private String loginToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String readSource(String... pathParts) throws Exception {
        Path root = Path.of("src", "main", "java", "vn", "edu", "crs", "smartcommunity");
        for (String pathPart : pathParts) {
            root = root.resolve(pathPart);
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .reduce("", String::concat);
        }
    }

    private record Flow(Long requestId, Long residentUserId, Long technicianUserId) {
    }

    private record LoginPayload(String email, String password) {
    }

    private record CreatePayload(String title, String description, String category, String priority) {
    }

    private record AssignPayload(Long technicianId) {
    }

    private record ResolvePayload(String resolutionNote) {
    }
}
