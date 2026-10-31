package com.infobro.apaar;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"postgres", "dev"})
@Testcontainers(disabledWithoutDocker = true)
class PostgresDatabaseTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    private static final String migrationPassword = UUID.randomUUID().toString();
    private static final String appPassword = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws Exception {
        // Use the actual manual script, not a copy of its grants. No password output.
        String script = Files.readString(Path.of("..", "db", "init", "create_app_user.sql"));
        try (Connection admin = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = admin.createStatement()) {
            statement.execute(script);
            statement.execute("ALTER ROLE apaar_migrator PASSWORD '" + migrationPassword + "'");
            statement.execute("ALTER ROLE apaar_app PASSWORD '" + appPassword + "'");
            Flyway.configure().dataSource(postgres.getJdbcUrl(), "apaar_migrator", migrationPassword)
                .baselineOnMigrate(false).load().migrate();
            statement.execute(script);
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "apaar_app");
        registry.add("spring.datasource.password", () -> appPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", () -> "apaar_migrator");
        registry.add("spring.flyway.password", () -> migrationPassword);
        registry.add("apaar.seed-demo-data", () -> "false");
        registry.add("apaar.jwt.secret", () -> java.util.Base64.getEncoder().encodeToString(new byte[48]));
    }

    @Autowired Flyway flyway;
    @Autowired Environment environment;
    @Autowired InstitutionRepository institutions;
    @Autowired ProgrammeRepository programmes;
    @Autowired StudentRepository students;

    private Student enrol(Institution institution, Programme programme, int year, String status) {
        return students.saveAndFlush(new Student("202611223344", "Student", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, year, "REGULAR", null, null, status, true));
    }
    private Institution institution(String code) {
        return institutions.save(new Institution(code, code, "State", "City", "ACTIVE"));
    }
    private Programme programme(Institution institution, String code) {
        return programmes.save(new Programme(institution, code, code, 2026, 100, 0));
    }

    @Test void migrationsAndHibernateValidationPassWithSeparateIdentities() throws Exception {
        assertThat(flyway.info().applied()).hasSize(3);
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), "apaar_app", appPassword);
             var statement = connection.createStatement()) {
            try (var rs = statement.executeQuery("select current_user")) {
                rs.next(); assertThat(rs.getString(1)).isEqualTo("apaar_app");
            }
            assertThatThrownBy(() -> statement.execute("create table public.forbidden_ddl(id bigint)"))
                .isInstanceOf(SQLException.class).extracting(e -> ((SQLException)e).getSQLState()).isEqualTo("42501");
        }
    }

    @Test @Transactional void partialUniqueIndexRejectsCountedDuplicateAcrossProgrammes() {
        Institution a = institution("A");
        Programme cs = programme(a, "CS"), ec = programme(a, "EC");
        enrol(a, cs, 2026, "VALID");
        enrol(a, ec, 2026, "DUPLICATE");
        enrol(a, ec, 2026, "ERROR");
        enrol(a, ec, 2025, "VALID");
        assertThatThrownBy(() -> enrol(a, ec, 2026, "VERIFIED"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test @Transactional void crossInstitutionQueryReturnsOnlyRequestedYear() {
        Institution a = institution("A"), b = institution("B");
        Programme cs = programme(a, "CS"), ec = programme(b, "EC");
        Student first = enrol(a, cs, 2026, "VALID"), second = enrol(b, ec, 2026, "VALID");
        enrol(a, cs, 2025, "VALID");
        assertThat(students.findCrossInstitutionDuplicates(2026)).extracting(Student::getId)
            .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test void appCannotMutateOrTruncateAppendOnlyTables() throws Exception {
        try (Connection app = DriverManager.getConnection(postgres.getJdbcUrl(), "apaar_app", appPassword);
             Statement statement = app.createStatement()) {
            // Even a zero-row mutation must fail with insufficient_privilege, not a constraint error.
            statement.execute("INSERT INTO audit_logs(actor,role,action,entity,timestamp) VALUES ('test','test','test','test',CURRENT_TIMESTAMP)");
            for (String table : new String[]{"audit_logs", "student_history", "verification_history"}) {
                for (String sql : new String[]{"UPDATE " + table + " SET id=id", "DELETE FROM " + table, "TRUNCATE " + table})
                    assertThatThrownBy(() -> statement.execute(sql)).isInstanceOf(SQLException.class)
                        .extracting(e -> ((SQLException)e).getSQLState()).isEqualTo("42501");
            }
        }
    }
}
