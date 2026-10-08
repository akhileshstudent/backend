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

import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AvailabilityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void occupancyChangesAreBoundedOwnedAndTimestamped() throws Exception {
        JsonNode login=mapper.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("email",DemoAccounts.FIRST_EMAIL,"password",DemoAccounts.PASSWORD))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String token=login.path("token").asText();
        mvc.perform(post("/api/manager/rooms/1/occupancy").header("Authorization","Bearer "+token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":-1}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/manager/rooms/1/occupancy").header("Authorization","Bearer "+token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(1))
            .andExpect(jsonPath("$.availabilityUpdatedAt").isNotEmpty());
        mvc.perform(post("/api/manager/hostels/1/rooms/confirm-availability").header("Authorization","Bearer "+token))
            .andExpect(status().isNoContent());
    }
}
