package com.infobro.apaar;

import com.infobro.apaar.config.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

/** Starts a real embedded HTTP server; reads only, creates no accounts or records. */
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_DB_TESTS", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("postgres")
class ExternalHttpStartupTest {
    @Autowired TestRestTemplate http;
    @Autowired JwtTokenProvider jwt;

    @Test void realHttpServerServesSwaggerAndAuthenticatedFrontendEndpoints() {
        assertThat(http.getForEntity("/v3/api-docs", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(http.getForEntity("/api/students", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.generateToken("verification@example.invalid", "ROLE_AICTE_REGULATOR", null, "Read-only verification"));
        for (String endpoint : new String[]{"/api/institution/all", "/api/institution/programmes", "/api/students", "/api/audit/logs"}) {
            var response = http.exchange(endpoint, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            assertThat(response.getStatusCode()).as(endpoint).isEqualTo(HttpStatus.OK);
        }
    }
}
