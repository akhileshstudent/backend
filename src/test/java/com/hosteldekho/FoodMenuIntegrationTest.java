package com.hosteldekho;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class FoodMenuIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void foodFiltersCombineAndSeededMenuHasSevenDays() throws Exception {
        mvc.perform(get("/api/hostels").param("foodIncluded","false").param("foodType","VEG"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Green Leaf Ladies Hostel"));
        mvc.perform(get("/api/hostels/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.foodType").value("VEG"))
            .andExpect(jsonPath("$.weeklyMenu.length()").value(7));
    }
}
