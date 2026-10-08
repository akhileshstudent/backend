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
class HostelCostIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void firstMonthTotalIncludesSpecifiedRentAndCharges() throws Exception {
        mvc.perform(get("/api/hostels/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rooms[0].breakdownComplete").value(true))
            .andExpect(jsonPath("$.rooms[0].firstMonthTotal").value(19400))
            .andExpect(jsonPath("$.rooms[0].costBreakdown.length()").value(5));
    }
}
