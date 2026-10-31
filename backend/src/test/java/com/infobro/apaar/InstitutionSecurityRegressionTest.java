package com.infobro.apaar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Disposable database only: never loads Supabase environment or runs external tests. */
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:security-scope;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "apaar.seed-demo-data=false", "apaar.cors.allowed-origins=https://frontend.example.com"})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@Transactional
class InstitutionSecurityRegressionTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired InstitutionRepository institutions;
    @Autowired ProgrammeRepository programmes;
    @Autowired StudentRepository students;
    @Autowired UserAccountRepository users;
    @Autowired ValidationIssueRepository issues;
    @Autowired UploadBatchRepository batches;
    @Autowired VerificationTokenRepository tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired com.infobro.apaar.config.JwtTokenProvider jwt;
    @Autowired org.springframework.core.env.Environment environment;
    Institution own, foreign;
    Programme ownProgramme, foreignProgramme;
    Student ownStudent, foreignStudent;
    ValidationIssue ownIssue, foreignIssue;
    static final String TEST_PASSWORD="OnlyForDisposableSecurityTests";

    @BeforeEach void fixtures() {
        own=institutions.save(new Institution("SCOPE_A","Test A","Test State","Test City","ACTIVE"));
        foreign=institutions.save(new Institution("SCOPE_B","Test B","Test State","Test City","ACTIVE"));
        ownProgramme=programmes.save(new Programme(own,"Programme A","A",2026,100,0));
        foreignProgramme=programmes.save(new Programme(foreign,"Programme B","B",2026,100,0));
        ownStudent=students.save(new Student("202612340001","Student A",LocalDate.of(2005,1,1),"Other",ownProgramme,own,2026,"REGULAR",null,null,"VALID",true));
        foreignStudent=students.save(new Student("202612340002","Student B",LocalDate.of(2005,1,1),"Other",foreignProgramme,foreign,2026,"REGULAR",null,null,"VALID",true));
        ownIssue=issues.save(new ValidationIssue(ownStudent,null,"FORMAT_ERROR","ERROR","Test issue",null,null));
        foreignIssue=issues.save(new ValidationIssue(foreignStudent,null,"FORMAT_ERROR","ERROR","Test issue",null,null));
        String hash=encoder.encode(TEST_PASSWORD);
        users.save(new UserAccount("admin@example.test",hash,"Test Admin","ROLE_INSTITUTION_ADMIN",own));
        users.save(new UserAccount("regulator@example.test",hash,"Test Regulator","ROLE_AICTE_REGULATOR",null));
        users.save(new UserAccount("verifier@example.test",hash,"Test Verifier","ROLE_AUTHORIZED_VERIFIER",null));
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void studentReadEditExportAndForeignProgrammeAreDenied() throws Exception {
        http.perform(get("/api/students/"+foreignStudent.getId())).andExpect(status().isForbidden());
        http.perform(put("/api/students/"+foreignStudent.getId()).contentType("application/json").content("{\"name\":\"Forbidden\"}")).andExpect(status().isForbidden());
        http.perform(get("/api/students").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(get("/api/students/export").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(put("/api/students/"+ownStudent.getId()).contentType("application/json").content(json.writeValueAsString(Map.of("programmeId",foreignProgramme.getId())))).andExpect(status().isForbidden());
        http.perform(post("/api/students").contentType("application/json").content(json.writeValueAsString(Map.of("name","Forbidden","programmeId",foreignProgramme.getId())))).andExpect(status().isForbidden());
        assertThat(foreignStudent.getName()).isEqualTo("Student B");
        assertThat(students.count()).isEqualTo(2);
        assertThat(ownStudent.getProgramme().getId()).isEqualTo(ownProgramme.getId());
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void uploadBatchIssuesAndTokenOwnershipAreEnforcedBeforeWrites() throws Exception {
        var file=new MockMultipartFile("file","test.csv","text/csv","name,apaar_id\nTest,202612340003".getBytes());
        http.perform(multipart("/api/upload").file(file).param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(get("/api/upload/batches").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(get("/api/validation/issues").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(post("/api/validation/issues/"+foreignIssue.getId()+"/resolve").contentType("application/json").content("{}")).andExpect(status().isForbidden());
        http.perform(post("/api/validation/issues/"+ownIssue.getId()+"/resolve").contentType("application/json").content(json.writeValueAsString(Map.of("programmeId",foreignProgramme.getId())))).andExpect(status().isForbidden());
        http.perform(post("/api/tokens/generate").contentType("application/json").content(json.writeValueAsString(Map.of("studentId",foreignStudent.getId())))).andExpect(status().isForbidden());
        assertThat(batches.count()).isZero();
        assertThat(tokens.count()).isZero();
        assertThat(ownIssue.getStatus()).isEqualTo("OPEN");
        assertThat(foreignIssue.getStatus()).isEqualTo("OPEN");
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void institutionListsProgrammesStatsAndUnfilteredListsStayInOwnScope() throws Exception {
        http.perform(get("/api/institution/programmes").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(get("/api/institution/stats").param("institutionId",foreign.getId().toString())).andExpect(status().isForbidden());
        http.perform(get("/api/institution/all")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].code").value("SCOPE_A"));
        http.perform(get("/api/students")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].name").value("Student A"));
        http.perform(get("/api/students/"+ownStudent.getId())).andExpect(status().isOk());
        http.perform(get("/api/validation/issues")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void batchOnlyIssuesRemainVisibleButMixedInstitutionIssuesAreDenied() throws Exception {
        UploadBatch ownBatch=batches.save(new UploadBatch(own,"own.csv",0,0,0,0,"Test","COMPLETED"));
        UploadBatch foreignBatch=batches.save(new UploadBatch(foreign,"foreign.csv",0,0,0,0,"Test","COMPLETED"));
        issues.save(new ValidationIssue(null,ownBatch,"FORMAT_ERROR","ERROR","Batch only",null,null));
        var mixed=issues.save(new ValidationIssue(ownStudent,foreignBatch,"FORMAT_ERROR","ERROR","Mixed owners",null,null));
        http.perform(get("/api/validation/issues")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        http.perform(post("/api/validation/issues/"+mixed.getId()+"/resolve").contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void missingInstitutionFailsClosedInsteadOfFallingBackToAllOrFirstInstitution() throws Exception {
        UserAccount admin=users.findByEmail("admin@example.test").orElseThrow();admin.setInstitution(null);users.save(admin);
        for(String path:new String[]{"/api/students","/api/students/export","/api/institution/all","/api/institution/current","/api/validation/issues","/api/upload/batches"})
            http.perform(get(path)).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username="regulator@example.test",roles="AICTE_REGULATOR")
    void regulatorRetainsCrossInstitutionReadAccess() throws Exception {
        http.perform(get("/api/students/"+foreignStudent.getId())).andExpect(status().isOk());
        http.perform(get("/api/students")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        http.perform(get("/api/institution/all")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        http.perform(get("/api/institution/programmes").param("institutionId",foreign.getId().toString())).andExpect(status().isOk());
    }

    @Test @WithMockUser(username="verifier@example.test",roles="AUTHORIZED_VERIFIER")
    void verifierCannotAccessInstitutionStudentAuditOrTokenIssuanceRoutes() throws Exception {
        for(String path:new String[]{"/api/students","/api/institution/all","/api/audit/logs"}) http.perform(get(path)).andExpect(status().isForbidden());
        http.perform(post("/api/tokens/generate").contentType("application/json").content(json.writeValueAsString(Map.of("studentId",ownStudent.getId())))).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void withdrawalRevokesExistingTokensAndReconsentDoesNotReactivateThem() throws Exception {
        String body=http.perform(post("/api/tokens/generate").contentType("application/json").content(json.writeValueAsString(Map.of("studentId",ownStudent.getId(),"allowedFields","NAME,APAAR"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token=json.readTree(body).get("token").asText();
        http.perform(get("/api/verify/"+token).with(anonymous())).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
        http.perform(put("/api/students/"+ownStudent.getId()).contentType("application/json").content("{\"consentGiven\":false}")).andExpect(status().isOk());
        assertThat(students.findById(ownStudent.getId()).orElseThrow().getConsentGiven()).isFalse();
        assertThat(tokens.findByToken(token).orElseThrow().getActive()).isFalse();
        http.perform(get("/api/verify/"+token).with(anonymous())).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false)).andExpect(jsonPath("$.studentName").doesNotExist());
        http.perform(put("/api/students/"+ownStudent.getId()).contentType("application/json").content("{\"consentGiven\":true}")).andExpect(status().isOk());
        http.perform(get("/api/verify/"+token).with(anonymous())).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false));
    }

    @Test void verificationRechecksConsentForAnOtherwiseActiveLegacyToken() throws Exception {
        ownStudent.setConsentGiven(false);students.save(ownStudent);
        tokens.save(new VerificationToken(ownStudent,"legacy-consent-token",LocalDateTime.now().plusHours(1),"NAME,DOB,APAAR","Test"));
        http.perform(get("/api/verify/legacy-consent-token").with(anonymous())).andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false)).andExpect(jsonPath("$.studentName").doesNotExist()).andExpect(jsonPath("$.dob").doesNotExist()).andExpect(jsonPath("$.maskedApaarId").doesNotExist());
    }

    @Test void threeExistingRolesStillUseRealBcryptLoginAndJwtProfileFlow() throws Exception {
        for(String email:new String[]{"admin@example.test","regulator@example.test","verifier@example.test"}) {
            String response=http.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsString(Map.of("email",email,"password",TEST_PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            String token=json.readTree(response).get("token").asText();
            http.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        }
    }

    @Test void signedTokenCannotAuthenticateMissingDisabledOrRoleChangedAccounts() throws Exception {
        String missing=jwt.generateToken("missing@example.test","ROLE_AICTE_REGULATOR",null,"Missing");
        http.perform(get("/api/students").header("Authorization","Bearer "+missing)).andExpect(status().isUnauthorized());
        UserAccount admin=users.findByEmail("admin@example.test").orElseThrow();
        String token=jwt.generateToken(admin.getEmail(),admin.getRole(),own.getId(),admin.getFullName());
        admin.setActive(false);users.save(admin);
        http.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        admin.setActive(true);admin.setRole("ROLE_AUTHORIZED_VERIFIER");users.save(admin);
        http.perform(get("/api/students").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }

    @Test void invalidJwtAndIncorrectPasswordAreRejected() throws Exception {
        http.perform(get("/api/students").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        http.perform(post("/api/auth/login").contentType("application/json").content(json.writeValueAsString(Map.of("email","admin@example.test","password","incorrect"))))
            .andExpect(status().isUnauthorized());
    }

    @Test void expiredJwtCannotAccessTheProfileEndpoint() throws Exception {
        var key=io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(environment.getProperty("apaar.jwt.secret")));
        String expired=io.jsonwebtoken.Jwts.builder().subject("admin@example.test").claim("role","ROLE_INSTITUTION_ADMIN")
            .claim("institutionId",own.getId()).expiration(new java.util.Date(System.currentTimeMillis()-60_000))
            .signWith(key,io.jsonwebtoken.Jwts.SIG.HS256).compact();
        http.perform(get("/api/auth/me").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
    }

    @Test void healthIsPublicAndCorsAllowsOnlyConfiguredOriginIncludingMultipartPreflight() throws Exception {
        http.perform(get("/api/health").with(anonymous())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        http.perform(options("/api/upload").header("Origin","https://frontend.example.com").header("Access-Control-Request-Method","POST")
            .header("Access-Control-Request-Headers","authorization,content-type")).andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin","https://frontend.example.com"));
        http.perform(options("/api/upload").header("Origin","https://untrusted.example.com").header("Access-Control-Request-Method","POST"))
            .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username="admin@example.test",roles="INSTITUTION_ADMIN")
    void missingConsentIsNotImplicitlyGrantedAndNonCsvUploadIsRejected() throws Exception {
        String response=http.perform(post("/api/students").contentType("application/json").content(json.writeValueAsString(Map.of(
            "name","No implicit consent","programmeId",ownProgramme.getId(),"apaarId","202612340003","dob","2005-01-01"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.consentGiven").value(false)).andReturn().getResponse().getContentAsString();
        long id=json.readTree(response).get("id").asLong();
        http.perform(post("/api/tokens/generate").contentType("application/json").content(json.writeValueAsString(Map.of("studentId",id))))
            .andExpect(status().isForbidden());
        var file=new MockMultipartFile("file","invalid.exe","application/octet-stream","invalid".getBytes());
        http.perform(multipart("/api/upload").file(file)).andExpect(status().isBadRequest());
        assertThat(batches.count()).isZero();
    }
}
