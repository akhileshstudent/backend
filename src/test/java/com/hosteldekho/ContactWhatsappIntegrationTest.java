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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class ContactWhatsappIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void managerWhatsappIsNormalizedToInternationalDigits() throws Exception {
        JsonNode login=mapper.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("email",DemoAccounts.FIRST_EMAIL,"password",DemoAccounts.PASSWORD))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        Map<String,Object> hostel=Map.ofEntries(
            Map.entry("name","WhatsApp Test Hostel"),Map.entry("address","Test Road"),Map.entry("locality","Jodimatla"),
            Map.entry("city","Visakhapatnam"),Map.entry("latitude",17.7),Map.entry("longitude",83.3),
            Map.entry("genderType","MALE"),Map.entry("coLiving",false),Map.entry("contactName","Manager"),
            Map.entry("contactPhone","9876543210"),Map.entry("contactWhatsapp","987 654 3210"),
            Map.entry("contactEmail","manager@example.test"),Map.entry("imageUrls",java.util.List.of()),
            Map.entry("colleges",java.util.List.of()));
        mvc.perform(post("/api/manager/hostels").header("Authorization","Bearer "+login.path("token").asText())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(hostel)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.contactWhatsapp").value("919876543210"));
    }
}
