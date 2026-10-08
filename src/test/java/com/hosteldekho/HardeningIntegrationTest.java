package com.hosteldekho;

import com.hosteldekho.config.DemoAccounts;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hosteldekho-hardening;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class HardeningIntegrationTest {
    private static final String SECRET = "change-this-development-secret-key-to-a-long-random-value-2026";

    @Autowired
    MockMvc mvc;

    @Test
    void malformedAndOversizedAuthenticationInputsUseStandardBadRequestJson() throws Exception {
        mvc.perform(post("/api/auth/login").with(remote("198.51.100.30"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":12,\"password\":[]}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request")).andExpect(jsonPath("$.message").exists());

        String longName = "x".repeat(121);
        mvc.perform(post("/api/auth/register").with(remote("198.51.100.31"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + longName + "\",\"email\":\"long@example.test\",\"password\":\"secure123\",\"phone\":\"9876543210\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void missingMalformedAndExpiredTokensAreUnauthorized() throws Exception {
        mvc.perform(get("/api/manager/hostels")).andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/api/manager/hostels").header("Authorization", "Bearer definitely-not-a-jwt"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));

        String expired = Jwts.builder().subject(DemoAccounts.FIRST_EMAIL)
            .issuedAt(new Date(System.currentTimeMillis() - 120_000))
            .expiration(new Date(System.currentTimeMillis() - 60_000))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(get("/api/manager/hostels").header("Authorization", "Bearer " + expired))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void repeatedInvalidLoginsAreTemporarilyRateLimited() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            mvc.perform(post("/api/auth/login").with(remote("198.51.100.32"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + DemoAccounts.FIRST_EMAIL + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(remote("198.51.100.32"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + DemoAccounts.FIRST_EMAIL + "\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void invalidFiltersAndUnknownIdsReturnStandardClientErrors() throws Exception {
        mvc.perform(get("/api/hostels").param("acType", "AIR_CONDITIONED"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("A request parameter or path value is invalid"));
        mvc.perform(get("/api/hostels/99999999"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private RequestPostProcessor remote(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }
}
