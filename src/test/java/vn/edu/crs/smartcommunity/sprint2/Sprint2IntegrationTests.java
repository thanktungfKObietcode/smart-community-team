package vn.edu.crs.smartcommunity.sprint2;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
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
import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;
import vn.edu.crs.smartcommunity.notification.internal.repository.NotificationRepository;
import vn.edu.crs.smartcommunity.property.api.BuildingInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPass;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPassStatus;
import vn.edu.crs.smartcommunity.visitor.internal.repository.VisitorPassRepository;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class Sprint2IntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private IdentityLookup identityLookup;
    @Autowired private PropertyLookup propertyLookup;
    @Autowired private VisitorPassRepository visitorPassRepository;
    @Autowired private NotificationRepository notificationRepository;

    @Test
    void authenticatedUserCanListFacilities() throws Exception {
        mockMvc.perform(get("/api/facilities").header("Authorization", "Bearer " + token("resident@test.com")))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotListFacilities() throws Exception {
        mockMvc.perform(get("/api/facilities")).andExpect(status().isUnauthorized());
    }

    @Test
    void residentCannotCreateFacility() throws Exception {
        mockMvc.perform(post("/api/management/facilities")
                        .header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(facilityJson(unique("RES"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateFacilityAndDuplicateCodeIsRejected() throws Exception {
        String code = unique("FAC");
        String body = facilityJson(code);
        mockMvc.perform(post("/api/management/facilities").header("Authorization", "Bearer " + token("manager@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(code));
        mockMvc.perform(post("/api/management/facilities").header("Authorization", "Bearer " + token("manager@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void residentCanCreateConfirmedBookingAndReceivesNotification() throws Exception {
        Long facilityId = createFacility("BOOK");
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        String response = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(bookingJson(facilityId, start, start.plusHours(1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.code").isString()).andReturn().getResponse().getContentAsString();
        JsonNode booking = objectMapper.readTree(response);
        assertTrue(notificationRepository.findByUserIdOrderByCreatedAtDesc(residentId()).stream()
                .anyMatch(n -> n.getType() == NotificationType.BOOKING_CONFIRMED
                        && n.getMessage().contains(booking.get("facilityId").asText())) ||
                notificationRepository.findByUserIdOrderByCreatedAtDesc(residentId()).stream()
                        .anyMatch(n -> n.getType() == NotificationType.BOOKING_CONFIRMED));
    }

    @Test
    void overlapIsRejectedButAdjacentBookingIsAllowed() throws Exception {
        Long facilityId = createFacility("OVERLAP");
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        createBooking(facilityId, start, start.plusHours(1));
        mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(bookingJson(facilityId, start.plusMinutes(30), start.plusHours(2))))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(bookingJson(facilityId, start.plusHours(1), start.plusHours(2))))
                .andExpect(status().isOk());
    }

    @Test
    void unavailableFacilityCannotBeBooked() throws Exception {
        Long facilityId = createFacility("UNAVAILABLE");
        mockMvc.perform(patch("/api/management/facilities/{id}", facilityId)
                        .header("Authorization", "Bearer " + token("manager@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk());
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(bookingJson(facilityId, start, start.plusHours(1))))
                .andExpect(status().isConflict());
    }

    @Test
    void bookingDoesNotTrustResidentIdFromRequest() throws Exception {
        Long facilityId = createFacility("SCALAR");
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        String response = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("facilityId", facilityId, "startTime", start, "endTime", start.plusHours(1), "residentId", 999999))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(objectMapper.readTree(response).get("residentId").asLong() != 999999);
    }

    @Test
    void residentOwnBookingCancellationWorks() throws Exception {
        Long facilityId = createFacility("CANCEL");
        LocalDateTime start = LocalDateTime.now().plusHours(3);
        JsonNode booking = objectMapper.readTree(createBooking(facilityId, start, start.plusHours(1)));
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.get("id").asLong())
                        .header("Authorization", "Bearer " + token("resident@test.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void residentCanCreatePassWithServerGeneratedCodeAndListOwnPasses() throws Exception {
        String response = createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2));
        JsonNode pass = objectMapper.readTree(response);
        assertNotNull(pass.get("code"));
        assertTrue(pass.get("code").asText().startsWith("VP-"));
        mockMvc.perform(get("/api/visitor-passes/my").header("Authorization", "Bearer " + token("resident@test.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + pass.get("id").asLong() + ")]").exists());
    }

    @Test
    void securityCanVerifyAndCheckInButResidentCannot() throws Exception {
        JsonNode pass = objectMapper.readTree(createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2)));
        mockMvc.perform(get("/api/security/visitor-passes/{code}", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.visitorName").value("Visitor Demo"));
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("resident@test.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_IN"));
        assertTrue(notificationRepository.findByUserIdOrderByCreatedAtDesc(residentId()).stream()
                .anyMatch(n -> n.getType() == NotificationType.VISITOR_CHECKED_IN));
    }

    @Test
    void visitorPassCannotBeCheckedInTwiceAndCanBeCheckedOut() throws Exception {
        JsonNode pass = objectMapper.readTree(createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2)));
        checkIn(pass.get("code").asText());
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-out", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_OUT"));
    }

    @Test
    void visitorPassCannotBeCheckedOutBeforeCheckIn() throws Exception {
        JsonNode pass = objectMapper.readTree(createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2)));
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-out", pass.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelledAndExpiredPassesCannotCheckIn() throws Exception {
        JsonNode cancelled = objectMapper.readTree(createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2)));
        mockMvc.perform(patch("/api/visitor-passes/{id}/cancel", cancelled.get("id").asLong())
                        .header("Authorization", "Bearer " + token("resident@test.com")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", cancelled.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isConflict());

        JsonNode expired = objectMapper.readTree(createPass(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(2)));
        VisitorPass entity = visitorPassRepository.findById(expired.get("id").asLong()).orElseThrow();
        entity.setValidUntil(LocalDateTime.now().minusMinutes(1));
        visitorPassRepository.saveAndFlush(entity);
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", expired.get("code").asText())
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isConflict());
        assertTrue(visitorPassRepository.findById(entity.getId()).orElseThrow().getStatus() == VisitorPassStatus.EXPIRED);
    }

    @Test
    void moduleBoundariesDoNotImportInternalRepositoriesOrExposeEntitiesInEvents() throws Exception {
        String source = readSource("booking");
        String visitor = readSource("visitor");
        String events = readSource("booking", "api") + readSource("visitor", "api");
        assertFalse(source.contains("facility.internal.repository"));
        assertFalse(source.contains("resident.internal.repository"));
        assertFalse(visitor.contains("resident.internal.repository"));
        assertFalse(visitor.contains("property.internal.repository"));
        assertFalse(events.contains("internal.entity"));
    }

    private Long createFacility(String prefix) throws Exception {
        String response = mockMvc.perform(post("/api/management/facilities")
                        .header("Authorization", "Bearer " + token("manager@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(facilityJson(unique(prefix))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String createBooking(Long facilityId, LocalDateTime start, LocalDateTime end) throws Exception {
        return mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(bookingJson(facilityId, start, end)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String createPass(LocalDateTime from, LocalDateTime until) throws Exception {
        return mockMvc.perform(post("/api/visitor-passes").header("Authorization", "Bearer " + token("resident@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                                "visitorName", "Visitor Demo", "visitorPhone", "0912345678",
                                "validFrom", from, "validUntil", until))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private void checkIn(String code) throws Exception {
        mockMvc.perform(post("/api/security/visitor-passes/{code}/check-in", code)
                        .header("Authorization", "Bearer " + token("security@test.com")))
                .andExpect(status().isOk());
    }

    private String facilityJson(String code) throws Exception {
        BuildingInfo building = propertyLookup.getBuildingByCode("A2").orElseThrow();
        return json(Map.of("code", code, "name", "Test Facility", "description", "Test", "buildingId", building.id(),
                "type", "OTHER", "bookable", true, "openingTime", LocalTime.of(0, 0), "closingTime", LocalTime.of(23, 59)));
    }

    private String bookingJson(Long facilityId, LocalDateTime start, LocalDateTime end) throws Exception {
        return json(Map.of("facilityId", facilityId, "startTime", start, "endTime", end));
    }

    private String token(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "123456"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private Long residentId() {
        return identityLookup.getUserByEmail("resident@test.com").orElseThrow().userId();
    }

    private String unique(String prefix) { return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }

    private String json(Object value) throws Exception { return objectMapper.writeValueAsString(value); }

    private String readSource(String... parts) throws Exception {
        Path root = Path.of("src", "main", "java", "vn", "edu", "crs", "smartcommunity");
        for (String part : parts) root = root.resolve(part);
        try (var paths = Files.walk(root)) {
            return paths.filter(p -> p.toString().endsWith(".java")).map(p -> {
                try { return Files.readString(p); } catch (Exception e) { throw new IllegalStateException(e); }
            }).reduce("", String::concat);
        }
    }
}
