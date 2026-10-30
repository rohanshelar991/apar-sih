package com.infobro.apaar;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/** Explicit opt-in only. Uses injected credentials; every test record is rolled back. */
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_DB_TESTS", matches = "true")
@SpringBootTest
@ActiveProfiles("postgres")
class ExternalPostgresSmokeTest {
    @Autowired DataSource dataSource;
    @Autowired Environment environment;
    @Autowired Flyway flyway;
    @Autowired EntityManager entityManager;
    @Autowired InstitutionRepository institutions;
    @Autowired ProgrammeRepository programmes;
    @Autowired StudentRepository students;

    @Test void schemaValidationAndConnectivity() throws Exception {
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
        try (Connection connection = dataSource.getConnection(); Statement s = connection.createStatement()) {
            try (ResultSet rs = s.executeQuery("select current_user")) {
                rs.next(); assertThat(rs.getString(1)).isEqualTo("apaar_app");
            }
            try (ResultSet rs = s.executeQuery("select count(*) from information_schema.tables where table_schema='public' and table_name in ('institutions','programmes','students','upload_batches','validation_issues','student_history','verification_tokens','audit_logs','user_accounts','verification_requests')")) {
                rs.next(); assertThat(rs.getInt(1)).isEqualTo(10);
            }
            try (ResultSet rs = s.executeQuery("select count(*) from information_schema.table_constraints where constraint_schema='public' and constraint_type='FOREIGN KEY' and table_name in ('programmes','students','upload_batches','validation_issues','student_history','verification_tokens','user_accounts','verification_requests')")) {
                rs.next(); assertThat(rs.getInt(1)).isEqualTo(10);
            }
        }
    }

    private Student enrol(Institution institution, Programme programme, int year, String status) {
        return students.saveAndFlush(new Student("202699998878", "Database verification", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, year, "REGULAR", null, null, status, true));
    }

    @Test @Transactional void countedUniqueIndexAndYearFilteredReport() {
        String tag = UUID.randomUUID().toString().substring(0, 16);
        Institution a = institutions.saveAndFlush(new Institution("probe-a-" + tag, "Test A", "Test", "Test", "ACTIVE"));
        Institution b = institutions.saveAndFlush(new Institution("probe-b-" + tag, "Test B", "Test", "Test", "ACTIVE"));
        Programme cs = programmes.saveAndFlush(new Programme(a, "CS", "CS", 2026, 100, 0));
        Programme ec = programmes.saveAndFlush(new Programme(a, "EC", "EC", 2026, 100, 0));
        Programme other = programmes.saveAndFlush(new Programme(b, "CS", "CS", 2026, 100, 0));
        Student first = enrol(a, cs, 2026, "VALID");
        Student second = enrol(b, other, 2026, "VALID");
        enrol(a, cs, 2025, "VALID");
        assertThat(students.findCrossInstitutionDuplicates(2026)).extracting(Student::getId)
            .contains(first.getId(), second.getId());
        assertThat(students.findCrossInstitutionDuplicates(2026)).allMatch(s -> s.getAdmissionYear() == 2026);
        enrol(a, ec, 2026, "ERROR");
        enrol(a, ec, 2026, "DUPLICATE");
        assertThatThrownBy(() -> enrol(a, ec, 2026, "PENDING"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test @Transactional void crudAndRelationshipsRoundTrip() {
        Institution institution = institutions.saveAndFlush(new Institution("probe-" + UUID.randomUUID().toString().substring(0, 20),
            "Database verification", "Test", "Test", "ACTIVE"));
        Long institutionId = institution.getId();
        institution.setCity("Updated test city");
        institutions.saveAndFlush(institution);
        entityManager.clear();
        assertThat(institutions.findById(institutionId).orElseThrow().getCity()).isEqualTo("Updated test city");
        Programme programme = programmes.saveAndFlush(new Programme(institution, "Test programme", "TEST", 2026, 100, 0));
        Student student = students.saveAndFlush(new Student("202699998877", "Database verification", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, 2026, "REGULAR", null, null, "VALID", true));
        entityManager.clear();
        Student loaded = students.findById(student.getId()).orElseThrow();
        assertThat(loaded.getProgramme().getInstitution().getId()).isEqualTo(institutionId);
        students.deleteById(student.getId()); students.flush();
        assertThat(students.findById(student.getId())).isEmpty();
        programmes.deleteById(programme.getId()); programmes.flush();
        institutions.deleteById(institutionId); institutions.flush();
        assertThat(institutions.findById(institutionId)).isEmpty();
    }

    @Test void appendOnlyGrantsAndNoDdl() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement s = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                s.executeUpdate("INSERT INTO audit_logs(actor,role,action,entity,timestamp) VALUES ('verification-probe','TEST','CONNECTIVITY_TEST','TEST',CURRENT_TIMESTAMP)");
                for (String sql : new String[]{"UPDATE audit_logs SET id=id WHERE false", "DELETE FROM audit_logs WHERE false",
                    "UPDATE student_history SET id=id WHERE false", "DELETE FROM student_history WHERE false",
                    "CREATE TABLE public.forbidden_probe(id bigint)"}) {
                    Savepoint savepoint = connection.setSavepoint();
                    assertThatThrownBy(() -> s.execute(sql)).isInstanceOf(SQLException.class)
                        .extracting(e -> ((SQLException)e).getSQLState()).isEqualTo("42501");
                    connection.rollback(savepoint);
                }
            } finally { connection.rollback(); }
        }
    }
}
