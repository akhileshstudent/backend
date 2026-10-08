package com.hosteldekho;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hosteldekho.config.DemoAccounts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-api-coverage;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class ApiCoverageIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void publicAndManagerEndpointsCoverCrudBookingAndOwnership() throws Exception {
        mvc.perform(get("/api/colleges")).andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4))).andExpect(jsonPath("$[0].slug").value("andhra-university"))
            .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mvc.perform(get("/api/colleges/andhra-university")).andExpect(status().isOk())
            .andExpect(jsonPath("$.hostelCount").value(2));
        mvc.perform(get("/api/hostels").param("collegeId", "1").param("acType", "AC")
                .param("gender", "MALE").param("coLiving", "false").param("minPrice", "10000")
                .param("maxPrice", "11000").param("onlyVacant", "true").param("locality", "Jodimatla")
                .param("foodIncluded", "false").param("foodType", "BOTH"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/api/hostels").param("minPrice", "50000").param("maxPrice", "100"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/hostels/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.rooms[0].availabilityUpdatedAt").isNotEmpty())
            .andExpect(jsonPath("$.contactEmail").value(nullValue()))
            .andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.managerEmail").doesNotExist());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", "Coverage Manager", "email", "coverage-manager@example.test",
                    "password", "secure123", "phone", "9876543219"))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.manager.email").value("coverage-manager@example.test"));
        String managerA = login("coverage-manager@example.test", "secure123");
        String managerB = login(DemoAccounts.SECOND_EMAIL);
        mvc.perform(get("/api/manager/hostels").header("Authorization", bearer(managerA)))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/manager/booking-requests").header("Authorization", bearer(managerA)))
            .andExpect(status().isOk());

        String hostelBody = json(Map.ofEntries(
            Map.entry("name", "API coverage stay"), Map.entry("description", "Coverage test listing"),
            Map.entry("address", "10 Test Lane"), Map.entry("locality", "Jodimatla"),
            Map.entry("city", "Visakhapatnam"), Map.entry("latitude", 17.73), Map.entry("longitude", 83.32),
            Map.entry("genderType", "MALE"), Map.entry("coLiving", false), Map.entry("contactName", "Test Manager"),
            Map.entry("contactPhone", "9876543210"), Map.entry("contactEmail", "manager@example.test"),
            Map.entry("rules", "Quiet after 10 PM"), Map.entry("amenities", "WiFi"),
            Map.entry("imageUrls", List.of("https://example.test/hostel.jpg")),
            Map.entry("colleges", List.of(Map.of("collegeId", 1, "distanceKm", 0.5))),
            Map.entry("securityDeposit", 0), Map.entry("maintenanceChargePerMonth", 0),
            Map.entry("messChargePerMonth", 0), Map.entry("electricityPolicy", "INCLUDED"),
            Map.entry("electricityChargePerMonth", 0), Map.entry("lockInMonths", 0),
            Map.entry("noticePeriodDays", 0), Map.entry("messIncludedInRent", false),
            Map.entry("foodType", "NONE"), Map.entry("mealTimings", "Not applicable"),
            Map.entry("weeklyMenu", List.of(Map.of("dayOfWeek", "MONDAY", "breakfast", "Idli",
                "lunch", "Rice", "snacks", "Fruit", "dinner", "Chapati"))),
            Map.entry("contactWhatsapp", "9876543210")));
        JsonNode createdHostel = mapper.readTree(mvc.perform(post("/api/manager/hostels")
                .header("Authorization", bearer(managerA)).contentType(MediaType.APPLICATION_JSON).content(hostelBody))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.securityDeposit").value(0))
            .andReturn().getResponse().getContentAsString());
        long hostelId = createdHostel.path("id").asLong();
        mvc.perform(put("/api/manager/hostels/" + hostelId).header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content(hostelBody))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("API coverage stay"));
        mvc.perform(put("/api/manager/hostels/" + hostelId).header("Authorization", bearer(managerB))
                .contentType(MediaType.APPLICATION_JSON).content(hostelBody))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/manager/hostels/" + hostelId + "/rooms").header("Authorization", bearer(managerB))
                .contentType(MediaType.APPLICATION_JSON).content(roomBody("NO-ACCESS", 1)))
            .andExpect(status().isForbidden());

        JsonNode room = mapper.readTree(mvc.perform(post("/api/manager/hostels/" + hostelId + "/rooms")
                .header("Authorization", bearer(managerA)).contentType(MediaType.APPLICATION_JSON)
                .content(roomBody("COVER-1", 1))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.vacantBeds").value(1)).andReturn().getResponse().getContentAsString());
        long roomId = room.path("id").asLong();
        mvc.perform(put("/api/manager/hostels/" + hostelId + "/rooms/" + roomId)
                .header("Authorization", bearer(managerA)).contentType(MediaType.APPLICATION_JSON)
                .content(roomBody("COVER-1", 2)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.capacity").value(2));
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(1));
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(0));
        mvc.perform(post("/api/manager/hostels/" + hostelId + "/rooms/confirm-availability")
                .header("Authorization", bearer(managerA))).andExpect(status().isNoContent());

        String bookingBody = json(Map.of("studentName", "API Student", "phone", "9876543212",
            "email", "api-student@example.test", "moveInDate", LocalDate.now().plusDays(3).toString()));
        mvc.perform(post("/api/rooms/" + roomId + "/booking-requests").contentType(MediaType.APPLICATION_JSON).content(bookingBody))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.message").exists());
        JsonNode requests = mapper.readTree(mvc.perform(get("/api/manager/booking-requests")
                .header("Authorization", bearer(managerA))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        long requestId = requests.get(0).path("id").asLong();
        mvc.perform(put("/api/manager/booking-requests/" + requestId + "/approve")
                .header("Authorization", bearer(managerA))).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(put("/api/manager/booking-requests/" + requestId + "/approve")
                .header("Authorization", bearer(managerA))).andExpect(status().isConflict());
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(2));
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(1));
        mvc.perform(post("/api/manager/rooms/" + roomId + "/occupancy").header("Authorization", bearer(managerA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(0));

        long removableRoomId = mapper.readTree(mvc.perform(post("/api/manager/hostels/" + hostelId + "/rooms")
                .header("Authorization", bearer(managerA)).contentType(MediaType.APPLICATION_JSON)
                .content(roomBody("COVER-2", 2))).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString()).path("id").asLong();
        mvc.perform(delete("/api/manager/hostels/" + hostelId + "/rooms/" + removableRoomId)
                .header("Authorization", bearer(managerA))).andExpect(status().isNoContent());
        mvc.perform(post("/api/rooms/" + roomId + "/booking-requests")
                .contentType(MediaType.APPLICATION_JSON).content(bookingBody)).andExpect(status().isCreated());
        JsonNode pendingRequests = mapper.readTree(mvc.perform(get("/api/manager/booking-requests")
                .header("Authorization", bearer(managerA))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        long pendingRequestId = pendingRequests.findValuesAsText("id").stream().mapToLong(Long::parseLong)
            .filter(id -> id != requestId).findFirst().orElseThrow();
        mvc.perform(put("/api/manager/booking-requests/" + pendingRequestId + "/reject")
                .header("Authorization", bearer(managerA))).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(delete("/api/manager/hostels/" + hostelId).header("Authorization", bearer(managerA)))
            .andExpect(status().isConflict());
        JsonNode disposable = mapper.readTree(mvc.perform(post("/api/manager/hostels")
                .header("Authorization", bearer(managerA)).contentType(MediaType.APPLICATION_JSON)
                .content(hostelBody.replace("API coverage stay", "Disposable coverage stay")))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(delete("/api/manager/hostels/" + disposable.path("id").asLong())
                .header("Authorization", bearer(managerA))).andExpect(status().isNoContent());
        mvc.perform(get("/api/hostels/99999999")).andExpect(status().isNotFound());
    }

    private String login(String email) throws Exception { return login(email, DemoAccounts.PASSWORD); }

    private String login(String email, String password) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", password))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("token").asText();
    }

    private String bearer(String token) { return "Bearer " + token; }

    private String roomBody(String number, int capacity) throws Exception {
        return json(Map.of("roomNumber", number, "acType", "AC", "capacity", capacity, "occupied", 0,
            "pricePerMonth", 10000, "pricePerDay", 500, "description", "Coverage room", "amenities", "WiFi",
            "imageUrls", List.of("https://example.test/room.jpg")));
    }

    private String json(Object value) throws Exception { return mapper.writeValueAsString(value); }
}
