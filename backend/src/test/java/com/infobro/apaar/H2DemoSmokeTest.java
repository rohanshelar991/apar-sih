package com.infobro.apaar;

import com.infobro.apaar.repository.InstitutionRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:demo-smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "apaar.seed-demo-data=true"
})
@org.springframework.test.context.ActiveProfiles("h2")
class H2DemoSmokeTest {
    @Autowired Environment environment;
    @Autowired InstitutionRepository institutions;
    @Autowired StudentRepository students;
    @Autowired UserAccountRepository accounts;

    @Test void explicitDemoStartsAndSeedsWithoutFlyway() {
        assertThat(environment.getDefaultProfiles()).contains("postgres");
        assertThat(environment.getActiveProfiles()).contains("h2");
        assertThat(environment.getProperty("spring.flyway.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
        assertThat(institutions.count()).isEqualTo(10);
        assertThat(students.count()).isGreaterThan(3000);
        assertThat(accounts.count()).isEqualTo(3);
    }
}
