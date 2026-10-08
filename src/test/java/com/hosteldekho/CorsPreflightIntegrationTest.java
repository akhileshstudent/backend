package com.hosteldekho;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "SPRING_DATASOURCE_URL=jdbc:h2:mem:hosteldekho-cors;DB_CLOSE_DELAY=-1",
    "SPRING_DATASOURCE_USERNAME=sa",
    "SPRING_DATASOURCE_PASSWORD=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "JWT_SECRET=cors-integration-test-secret-with-32-chars",
    "CORS_ORIGINS=https://frontend-xvox.onrender.com"
})
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class CorsPreflightIntegrationTest {
    private static final String FRONTEND_ORIGIN = "https://frontend-xvox.onrender.com";

    @Autowired
    private MockMvc mvc;

    @Test
    void productionCorsAllowsFrontendPreflightWithoutAuthentication() throws Exception {
        mvc.perform(options("/api/manager/hostels")
                .header("Origin", FRONTEND_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")))
            .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }
}
