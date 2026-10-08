package com.hosteldekho;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class CollegePagesIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void collegeSlugReturnsComputedStatsAndUnknownSlugIsNotFound() throws Exception {
        mvc.perform(get("/api/colleges/andhra-university"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Andhra University"))
            .andExpect(jsonPath("$.hostelCount").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.vacantBeds").value(greaterThanOrEqualTo(12)))
            .andExpect(jsonPath("$.minPricePerMonth").value(10000));
        mvc.perform(get("/api/colleges/not-a-college"))
            .andExpect(status().isNotFound());
    }
}
