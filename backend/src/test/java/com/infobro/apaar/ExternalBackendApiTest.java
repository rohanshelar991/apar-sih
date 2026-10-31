package com.infobro.apaar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobro.apaar.config.JwtTokenProvider;
import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import com.infobro.apaar.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_DB_TESTS", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@ActiveProfiles("postgres")
@Transactional
class ExternalBackendApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired InstitutionRepository institutions;
    @Autowired ProgrammeRepository programmes;
    @Autowired StudentRepository students;
    @Autowired UserAccountRepository users;
    @Autowired AuditLogRepository audits;
    @Autowired StudentHistoryRepository history;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtTokenProvider jwt;
    @Autowired StudentService studentService;

    @org.junit.jupiter.api.AfterEach void clearSecurityContext() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }

    private Institution institution() {
        return institutions.saveAndFlush(new Institution("api-" + UUID.randomUUID().toString().substring(0, 20),
            "API test institution", "Test", "Test", "ACTIVE"));
    }
    private DTOs.StudentFormRequest form(Programme programme) {
        var form = new DTOs.StudentFormRequest();
        form.setApaarId("202688887771"); form.setName("Test Student");
        form.setDob(LocalDate.of(2005, 1, 1)); form.setGender("Other");
        form.setProgrammeId(programme.getId()); form.setAdmissionYear(2026);
        form.setCategory("REGULAR"); form.setConsentGiven(true);
        return form;
    }

    @Test void loginStudentWriteReadAndAuditHistory() throws Exception {
        Institution institution = institution();
        Programme programme = programmes.saveAndFlush(new Programme(institution, "CS", "CS", 2026, 100, 0));
        String password = UUID.randomUUID().toString();
        String email = UUID.randomUUID() + "@example.invalid";
        users.saveAndFlush(new UserAccount(email, encoder.encode(password), "Test admin", "ROLE_INSTITUTION_ADMIN", institution));
        var request = new DTOs.LoginRequest(); request.setEmail(email); request.setPassword(password);
        String loginBody = mvc.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsBytes(request)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.role").value("ROLE_INSTITUTION_ADMIN"))
            .andExpect(jsonPath("$.institutionId").value(institution.getId()))
            .andReturn().getResponse().getContentAsString();
        String bearer = "Bearer " + json.readTree(loginBody).get("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", bearer)).andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));
        var form = form(programme);
        JsonNode created = json.readTree(mvc.perform(post("/api/students").header("Authorization", bearer)
            .contentType("application/json").content(json.writeValueAsBytes(form)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.verificationStatus").value("VALID"))
            .andReturn().getResponse().getContentAsString());
        long id = created.get("id").asLong();
        mvc.perform(get("/api/students/{id}", id).header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.programmeId").value(programme.getId()));
        mvc.perform(get("/api/students").param("search", "Test Student").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/students").header("Authorization", bearer)).andExpect(status().isOk());
        mvc.perform(get("/api/students/export").header("Authorization", bearer)).andExpect(status().isOk());
        form.setName("Updated Student");
        mvc.perform(put("/api/students/{id}", id).header("Authorization", bearer).contentType("application/json")
            .content(json.writeValueAsBytes(form))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Student"));
        assertThat(history.findByStudentIdOrderByChangedAtDesc(id)).anyMatch(h -> h.getField().equals("name"));
        assertThat(audits.findAll().stream().filter(a -> email.equals(a.getActor())).map(AuditLog::getAction))
            .contains("LOGIN_SUCCESS", "CREATE_STUDENT", "UPDATE_STUDENT");
        mvc.perform(get("/api/institution/all").header("Authorization", bearer)).andExpect(status().isOk());
        mvc.perform(get("/api/institution/programmes").param("institutionId", institution.getId().toString())
            .header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].institution.id").value(institution.getId()));
        // Everything above, including logs/history, joins this test transaction and rolls back.
    }

    @Test void serviceValidationAndDuplicateRules() {
        Institution institution = institution();
        String email=UUID.randomUUID()+"@example.invalid";
        users.saveAndFlush(new UserAccount(email,encoder.encode(UUID.randomUUID().toString()),"Probe admin","ROLE_INSTITUTION_ADMIN",institution));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email,null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_INSTITUTION_ADMIN"))));
        Programme first = programmes.saveAndFlush(new Programme(institution, "CS", "CS", 2026, 100, 0));
        Programme second = programmes.saveAndFlush(new Programme(institution, "EC", "EC", 2026, 100, 0));
        var form = form(first);
        var original = studentService.createStudent(institution.getId(), form, "probe", "ROLE_INSTITUTION_ADMIN", "127.0.0.1");
        assertThat(original.getVerificationStatus()).isEqualTo("VALID");
        form.setProgrammeId(second.getId());
        var duplicate = studentService.createStudent(institution.getId(), form, "probe", "ROLE_INSTITUTION_ADMIN", "127.0.0.1");
        assertThat(duplicate.getVerificationStatus()).isEqualTo("DUPLICATE");
        assertThat(duplicate.getIssues()).anyMatch(i -> i.getRuleCode().equals("DUPLICATE_SAME_INSTITUTION"));
        form.setApaarId("invalid");
        var invalid = studentService.createStudent(institution.getId(), form, "probe", "ROLE_INSTITUTION_ADMIN", "127.0.0.1");
        assertThat(invalid.getVerificationStatus()).isEqualTo("ERROR");
        assertThat(invalid.getIssues()).anyMatch(i -> i.getRuleCode().equals("APAAR_FORMAT"));
    }

    @Test void verificationWorkflowDuplicateProtectionDecisionsHistoryAndNotifications() throws Exception {
        Institution institution = institution();
        Programme programme = programmes.saveAndFlush(new Programme(institution, "Workflow programme", "WF", 2026, 100, 0));
        Student student = students.saveAndFlush(new Student("202688880001", "Workflow student", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, 2026, "REGULAR", null, null, "PENDING", true));
        String adminEmail = UUID.randomUUID() + "@example.invalid";
        String reviewerEmail = UUID.randomUUID() + "@example.invalid";
        users.saveAndFlush(new UserAccount(adminEmail, encoder.encode(UUID.randomUUID().toString()), "Workflow admin", "ROLE_INSTITUTION_ADMIN", institution));
        users.saveAndFlush(new UserAccount(reviewerEmail, encoder.encode(UUID.randomUUID().toString()), "Workflow reviewer", "ROLE_AUTHORIZED_VERIFIER", null));
        String admin = "Bearer " + jwt.generateToken(adminEmail, "ROLE_INSTITUTION_ADMIN", institution.getId(), "Workflow admin");
        String reviewer = "Bearer " + jwt.generateToken(reviewerEmail, "ROLE_AUTHORIZED_VERIFIER", null, "Workflow reviewer");

        String body = mvc.perform(post("/api/verification/students/{id}/requests", student.getId()).header("Authorization", admin))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString();
        long caseId = json.readTree(body).get("id").asLong();
        mvc.perform(post("/api/verification/students/{id}/requests", student.getId()).header("Authorization", admin))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/verification/requests").header("Authorization", reviewer))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].studentId").value(student.getId()));
        mvc.perform(post("/api/verification/requests/{id}/decision", caseId).header("Authorization", reviewer)
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("status", "REJECTED", "remarks", " "))))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/verification/requests/{id}/decision", caseId).header("Authorization", reviewer)
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("status", "UNDER_REVIEW", "remarks", "Source record being checked."))))
            .andExpect(status().isOk());
        mvc.perform(post("/api/verification/requests/{id}/decision", caseId).header("Authorization", reviewer)
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("status", "APPROVED", "remarks", "Institutional record reviewed."))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/verification/requests/{id}/history", caseId).header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)));
        String notifications = mvc.perform(get("/api/verification/notifications").header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
            .andReturn().getResponse().getContentAsString();
        long notificationId = json.readTree(notifications).get(0).get("id").asLong();
        mvc.perform(post("/api/verification/notifications/{id}/read", notificationId).header("Authorization", admin))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/verification/notifications").header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].readAt").isNotEmpty());
        mvc.perform(get("/api/verification/requests").header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("APPROVED"));
        mvc.perform(get("/api/verification/requests").header("Authorization", reviewer)).andExpect(status().isOk());
        mvc.perform(get("/api/verification/requests").header("Authorization", "Bearer " + jwt.generateToken(
            "unregistered@example.invalid", "ROLE_AUTHORIZED_VERIFIER", null, "Unregistered")))
            .andExpect(status().isUnauthorized());
    }

    @Test void errorsAndConfiguredCors() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("Unauthorized"));
        users.saveAndFlush(new UserAccount("verifier-probe@example.invalid",encoder.encode(UUID.randomUUID().toString()),"Probe verifier","ROLE_AUTHORIZED_VERIFIER",null));
        String verifier = "Bearer " + jwt.generateToken("verifier-probe@example.invalid", "ROLE_AUTHORIZED_VERIFIER", null, "Probe");
        mvc.perform(get("/api/students").header("Authorization", verifier)).andExpect(status().isForbidden());
        mvc.perform(options("/api/students").header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "GET")).andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/students").header("Origin", "https://untrusted.invalid")
            .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
        users.saveAndFlush(new UserAccount("regulator-probe@example.invalid",encoder.encode(UUID.randomUUID().toString()),"Probe regulator","ROLE_AICTE_REGULATOR",null));
        String regulator = "Bearer " + jwt.generateToken("regulator-probe@example.invalid", "ROLE_AICTE_REGULATOR", null, "Probe");
        mvc.perform(post("/api/students").header("Authorization", regulator).contentType("application/json").content("{"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/audit/logs").header("Authorization", regulator)).andExpect(status().isOk());
        audits.saveAndFlush(new AuditLog("ProbeActor", "ROLE_AICTE_REGULATOR", "PROBE", "Student", null, "127.0.0.1", "Test"));
        mvc.perform(get("/api/audit/logs").param("actor", "probeactor")
            .param("fromTime", "2000-01-01T00:00:00").param("toTime", "2099-01-01T00:00:00")
            .header("Authorization", regulator)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].actor").value("ProbeActor"));
        mvc.perform(get("/api/audit/export").header("Authorization", regulator)).andExpect(status().isOk());
    }

    @Test void programmeInstitutionCrudAndStudentDeleteUseExistingRepositories() {
        Institution institution = institution();
        institution.setCity("Updated city"); institutions.saveAndFlush(institution);
        assertThat(institutions.findById(institution.getId()).orElseThrow().getCity()).isEqualTo("Updated city");
        assertThat(institutions.findAll()).anyMatch(i -> i.getId().equals(institution.getId()));
        Programme programme = programmes.saveAndFlush(new Programme(institution, "CS", "CS", 2026, 100, 0));
        programme.setApprovedIntake(110); programmes.saveAndFlush(programme);
        assertThat(programmes.findByInstitutionId(institution.getId())).anyMatch(p -> p.getApprovedIntake() == 110);
        Student student = students.saveAndFlush(new Student("202688887772", "Delete probe", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, 2026, "REGULAR", null, null, "VALID", true));
        students.deleteById(student.getId()); students.flush();
        assertThat(students.findById(student.getId())).isEmpty();
        programmes.deleteById(programme.getId()); programmes.flush();
        institutions.deleteById(institution.getId()); institutions.flush();
        assertThat(institutions.findById(institution.getId())).isEmpty();
    }
}
