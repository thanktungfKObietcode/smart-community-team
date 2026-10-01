package vn.edu.crs.smartcommunity.phase2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Stream;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.property.internal.entity.Building;
import vn.edu.crs.smartcommunity.property.internal.repository.BuildingRepository;
import vn.edu.crs.smartcommunity.resident.internal.entity.Resident;
import vn.edu.crs.smartcommunity.resident.internal.repository.ResidentRepository;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class PropertyResidentIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private IdentityLookup identityLookup;

    @Test
    void authenticatedUserCanListBuildings() throws Exception {
        mockMvc.perform(get("/api/buildings")
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'A2')]").exists());
    }

    @Test
    void unauthenticatedUserCannotListBuildings() throws Exception {
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void residentCannotCreateBuilding() throws Exception {
        mockMvc.perform(post("/api/management/buildings")
                        .header("Authorization", "Bearer " + residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildingJson("RESIDENT-CANNOT-CREATE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateBuilding() throws Exception {
        String code = uniqueCode("PHASE2");

        mockMvc.perform(post("/api/management/buildings")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildingJson(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void duplicateBuildingCodeIsRejected() throws Exception {
        mockMvc.perform(post("/api/management/buildings")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildingJson("a2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Building code already exists"));
    }

    @Test
    void managerCanCreateApartment() throws Exception {
        Long buildingId = demoBuildingId();
        String unitNumber = uniqueUnit("A");

        mockMvc.perform(post("/api/management/buildings/{buildingId}/apartments", buildingId)
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apartmentJson(unitNumber, 12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitNumber").value(unitNumber))
                .andExpect(jsonPath("$.buildingId").value(buildingId));
    }

    @Test
    void duplicateApartmentUnitWithinBuildingIsRejected() throws Exception {
        Long buildingId = demoBuildingId();
        String unitNumber = uniqueUnit("D");
        MockHttpServletRequestBuilder request = post(
                "/api/management/buildings/{buildingId}/apartments", buildingId)
                .header("Authorization", "Bearer " + managerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(apartmentJson(unitNumber, 12));

        mockMvc.perform(request).andExpect(status().isOk());
        mockMvc.perform(post("/api/management/buildings/{buildingId}/apartments", buildingId)
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apartmentJson(unitNumber.toLowerCase(), 12)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Apartment unit already exists in this building"));
    }

    @Test
    void residentDemoProfileExists() {
        Long residentUserId = identityLookup.getUserByEmail("resident@test.com")
                .orElseThrow()
                .userId();
        Resident resident = residentRepository.findByUserId(residentUserId).orElse(null);
        assertNotNull(resident);
        assertTrue(resident.isActive());
    }

    @Test
    void residentMeReturnsAuthenticatedResidentsApartment() throws Exception {
        mockMvc.perform(get("/api/residents/me")
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(residentUserId()))
                .andExpect(jsonPath("$.email").value("resident@test.com"))
                .andExpect(jsonPath("$.residentType").value("OWNER"))
                .andExpect(jsonPath("$.apartment.unitNumber").value("A1205"))
                .andExpect(jsonPath("$.apartment.buildingCode").value("A2"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void residentMeDoesNotTrustUserIdSuppliedByClient() throws Exception {
        Long managerUserId = identityLookup.getUserByEmail("manager@test.com")
                .orElseThrow()
                .userId();

        mockMvc.perform(get("/api/residents/me")
                        .param("userId", managerUserId.toString())
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(residentUserId()))
                .andExpect(jsonPath("$.email").value("resident@test.com"));
    }

    @Test
    void nonResidentRoleCannotUseResidentMe() throws Exception {
        mockMvc.perform(get("/api/residents/me")
                        .header("Authorization", "Bearer " + managerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanListResidents() throws Exception {
        mockMvc.perform(get("/api/management/residents")
                        .header("Authorization", "Bearer " + managerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'resident@test.com')]").exists());
    }

    @Test
    void managementResidentEndpointRejectsExistingProfile() throws Exception {
        mockMvc.perform(post("/api/management/residents")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(residentJson(residentUserId(), demoApartmentId(), "OWNER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Resident profile already exists"));
    }

    @Test
    void managerCreatesResidentWithAnIdentityAccountAndSelectedApartment() throws Exception {
        String email = uniqueEmail("resident-workflow");

        String response = mockMvc.perform(post("/api/management/residents")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newResidentJson(email, demoApartmentId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Resident Workflow"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.apartment.id").value(demoApartmentId()))
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("userId").asLong();
        assertTrue(identityLookup.getUserById(userId).orElseThrow().roles().contains("RESIDENT"));
        assertTrue(residentRepository.findByUserId(userId).isPresent());
    }

    @Test
    void invalidApartmentDoesNotCreateAnOrphanResidentAccount() throws Exception {
        String email = uniqueEmail("missing-apartment");

        mockMvc.perform(post("/api/management/residents")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newResidentJson(email, Long.MAX_VALUE)))
                .andExpect(status().isNotFound());

        assertTrue(identityLookup.getUserByEmail(email).isEmpty());
    }

    @Test
    void residentWorkflowRejectsDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/management/residents")
                        .header("Authorization", "Bearer " + managerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newResidentJson("resident@test.com", demoApartmentId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void residentModuleDoesNotAccessIdentityOrPropertyInternalRepositories() throws Exception {
        String source = readResidentInternalSource();
        assertFalse(source.contains("identity.internal.repository"));
        assertFalse(source.contains("property.internal.repository"));
        assertFalse(source.contains("@ManyToOne"));
    }

    private Long demoBuildingId() {
        return buildingRepository.findByCodeIgnoreCase("A2").orElseThrow().getId();
    }

    private Long demoApartmentId() throws Exception {
        String response = mockMvc.perform(get("/api/buildings/{buildingId}/apartments", demoBuildingId())
                        .header("Authorization", "Bearer " + residentToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode apartments = objectMapper.readTree(response);
        for (JsonNode apartment : apartments) {
            if ("A1205".equals(apartment.get("unitNumber").asText())) {
                return apartment.get("id").asLong();
            }
        }
        throw new AssertionError("Demo apartment A1205 was not found");
    }

    private Long residentUserId() {
        return identityLookup.getUserByEmail("resident@test.com").orElseThrow().userId();
    }

    private String residentToken() throws Exception {
        return loginToken("resident@test.com", "123456");
    }

    private String managerToken() throws Exception {
        return loginToken("manager@test.com", "123456");
    }

    private String loginToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String buildingJson(String code) throws Exception {
        return objectMapper.writeValueAsString(
                new BuildingPayload(code, "Phase 2 Test Building", "Test address"));
    }

    private String apartmentJson(String unitNumber, int floorNumber) throws Exception {
        return objectMapper.writeValueAsString(new ApartmentPayload(unitNumber, floorNumber));
    }

    private String residentJson(Long userId, Long apartmentId, String residentType) throws Exception {
        return objectMapper.writeValueAsString(
                new ResidentPayload(userId, apartmentId, residentType, "0912345678", null));
    }

    private String newResidentJson(String email, Long apartmentId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "fullName", "Resident Workflow",
                "email", email,
                "initialPassword", "123456",
                "apartmentId", apartmentId,
                "residentType", "OWNER",
                "phone", "0912345678"));
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@test.com";
    }

    private String uniqueCode(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String uniqueUnit(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String readResidentInternalSource() throws Exception {
        Path root = Path.of("src", "main", "java", "vn", "edu", "crs", "smartcommunity", "resident", "internal");
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

    private record LoginPayload(String email, String password) {
    }

    private record BuildingPayload(String code, String name, String address) {
    }

    private record ApartmentPayload(String unitNumber, Integer floorNumber) {
    }

    private record ResidentPayload(
            Long userId,
            Long apartmentId,
            String residentType,
            String phone,
            String moveInDate) {
    }
}
