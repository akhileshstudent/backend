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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class HostelDekhoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void managersCanAuthenticateAndCompleteBookingWithoutOverfilling() throws Exception {
        mvc.perform(get("/api/hostels").param("collegeId", "1").param("acType", "AC")
                .param("gender", "MALE").param("minPrice", "5000").param("maxPrice", "20000").param("onlyVacant", "true"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].genderType").value("MALE"))
            .andExpect(jsonPath("$[0].imageUrls[0]").isNotEmpty());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", DemoAccounts.FIRST_EMAIL, "password", "wrong"))))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/api/manager/hostels")).andExpect(status().isUnauthorized());

        JsonNode firstLogin = login(DemoAccounts.FIRST_EMAIL, DemoAccounts.PASSWORD);
        String firstToken = firstLogin.path("token").asText();
        org.junit.jupiter.api.Assertions.assertTrue(firstLogin.path("manager").path("id").asLong()>0);
        String register = json(Map.of("name","Integration Manager","email","flow@hosteldekho.test","password","pass1234","phone","9876543219"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.manager.email").value("flow@hosteldekho.test"));

        Map<String,Object> hostel = Stream.of(new Object[][]{{"name","Test Student Stay"},{"description","Integration test listing"},{"address","1 Test Street"},{"locality","Jodimatla"},{"city","Visakhapatnam"},{"latitude",17.73},{"longitude",83.32},{"genderType","CO_ED"},{"coLiving",true},{"contactName","Integration Manager"},{"contactPhone","9876543219"},{"contactEmail","flow@hosteldekho.test"},{"rules","Keep shared spaces clean"},{"amenities","WiFi, Mess"},{"imageUrls",java.util.List.of("https://example.test/hostel.jpg")},{"colleges",new Object[]{Map.of("collegeId",1,"distanceKm",1.0)}}}).collect(Collectors.toMap(pair->(String)pair[0],pair->pair[1]));
        JsonNode created = mapper.readTree(mvc.perform(post("/api/manager/hostels").header("Authorization","Bearer "+firstToken).contentType(MediaType.APPLICATION_JSON).content(json(hostel)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long hostelId = created.path("id").asLong();

        Map<String,Object> room = Map.of("roomNumber","TEST-1","acType","AC","capacity",1,"occupied",0,"pricePerMonth",BigDecimalValue.of(10000),"pricePerDay",500,"description","Sunny private room","amenities","Attached bath, Wardrobe","imageUrls",java.util.List.of("https://example.test/room.jpg","data:image/jpeg;base64,AAAA"));
        JsonNode createdRoom = mapper.readTree(mvc.perform(post("/api/manager/hostels/"+hostelId+"/rooms").header("Authorization","Bearer "+firstToken).contentType(MediaType.APPLICATION_JSON).content(json(room)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("Sunny private room",createdRoom.path("description").asText());
        org.junit.jupiter.api.Assertions.assertEquals("https://example.test/room.jpg",createdRoom.path("imageUrls").get(0).asText());
        org.junit.jupiter.api.Assertions.assertEquals("data:image/jpeg;base64,AAAA",createdRoom.path("imageUrls").get(1).asText());
        long roomId=createdRoom.path("id").asLong();
        mvc.perform(get("/api/hostels/"+hostelId)).andExpect(status().isOk())
            .andExpect(jsonPath("$.rooms[0].description").value("Sunny private room"))
            .andExpect(jsonPath("$.rooms[0].imageUrls[0]").value("https://example.test/room.jpg"));
        Map<String,Object> booking=Map.of("studentName","Student One","phone","9876543212","email","student@example.test","moveInDate",LocalDate.now().plusDays(2).toString());
        String bookingJson=json(booking);
        mvc.perform(post("/api/rooms/"+roomId+"/booking-requests").contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isCreated());
        mvc.perform(post("/api/rooms/"+roomId+"/booking-requests").contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isCreated());
        JsonNode requests=mapper.readTree(mvc.perform(get("/api/manager/booking-requests").header("Authorization","Bearer "+firstToken)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long pendingId=0,secondId=0;
        for(JsonNode request:requests)if(request.path("roomId").asLong()==roomId){if(pendingId==0)pendingId=request.path("id").asLong();else secondId=request.path("id").asLong();}

        mvc.perform(put("/api/manager/booking-requests/"+pendingId+"/approve").header("Authorization","Bearer "+firstToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/hostels/"+hostelId)).andExpect(status().isOk())
            .andExpect(jsonPath("$.rooms[0].occupied").value(1)).andExpect(jsonPath("$.rooms[0].vacantBeds").value(0));
        mvc.perform(put("/api/manager/booking-requests/"+pendingId+"/approve").header("Authorization","Bearer "+firstToken))
            .andExpect(status().isConflict());
        mvc.perform(put("/api/manager/booking-requests/"+secondId+"/approve").header("Authorization","Bearer "+firstToken))
            .andExpect(status().isConflict());
        mvc.perform(put("/api/manager/booking-requests/"+secondId+"/reject").header("Authorization","Bearer "+firstToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(put("/api/manager/booking-requests/"+secondId+"/approve").header("Authorization","Bearer "+firstToken))
            .andExpect(status().isConflict());

        JsonNode secondLogin=login(DemoAccounts.SECOND_EMAIL,DemoAccounts.PASSWORD);
        JsonNode secondHostels=mapper.readTree(mvc.perform(get("/api/manager/hostels").header("Authorization","Bearer "+secondLogin.path("token").asText())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for(JsonNode secondHostel:secondHostels)org.junit.jupiter.api.Assertions.assertNotEquals(hostelId,secondHostel.path("id").asLong());
        mvc.perform(put("/api/manager/hostels/"+hostelId).header("Authorization","Bearer "+secondLogin.path("token").asText()).contentType(MediaType.APPLICATION_JSON).content(json(hostel)))
            .andExpect(status().isForbidden());
    }

    private JsonNode login(String email,String password) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json(Map.of("email",email,"password",password))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty()).andReturn().getResponse().getContentAsString());
    }
    private String json(Object value) throws Exception { return mapper.writeValueAsString(value); }
    private static final class BigDecimalValue { static java.math.BigDecimal of(long value){return java.math.BigDecimal.valueOf(value);} }
}
