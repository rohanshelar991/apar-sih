package com.infobro.apaar;

import com.infobro.apaar.adapter.ApaarAdapterResult;
import com.infobro.apaar.adapter.ApaarVerificationAdapter;
import com.infobro.apaar.entity.Institution;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.entity.Student;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.validation.ValidationEngine;
import com.infobro.apaar.validation.ValidationResult;
import com.infobro.apaar.validation.ValidationRuleCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationEngineTest {

    private Institution testInst;
    private Programme testProg;

    // Configurable test stub states
    private List<Student> apaarListResult = Collections.emptyList();
    private List<Student> sameInstResult = Collections.emptyList();
    private List<Student> crossInstResult = Collections.emptyList();
    private long currentProgEnrolmentCount = 0L;
    private ApaarAdapterResult customAdapterResult = null;

    private StudentRepository studentRepoStub;
    private ProgrammeRepository progRepoStub;
    private ApaarVerificationAdapter adapterStub;
    private ValidationEngine validationEngine;

    @BeforeEach
    void setUp() {
        apaarListResult = Collections.emptyList();
        sameInstResult = Collections.emptyList();
        crossInstResult = Collections.emptyList();
        currentProgEnrolmentCount = 0L;
        customAdapterResult = null;

        testInst = new Institution("INST001", "BMS College of Engineering", "Karnataka", "Bengaluru", "ACTIVE");
        testInst.setId(1L);

        testProg = new Programme(testInst, "B.Tech Computer Science & Engineering", "CSE", 2026, 120, 18);
        testProg.setId(10L);

        studentRepoStub = (StudentRepository) Proxy.newProxyInstance(
                StudentRepository.class.getClassLoader(),
                new Class<?>[]{StudentRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findByApaarId".equals(name)) {
                        return apaarListResult;
                    }
                    if ("findByApaarIdAndInstitutionIdAndAdmissionYearAndVerificationStatusIn".equals(name)) {
                        return sameInstResult;
                    }
                    if ("findByApaarIdAndAdmissionYear".equals(name)) {
                        return crossInstResult;
                    }
                    if ("countByProgrammeIdAndVerificationStatusIn".equals(name)) {
                        return currentProgEnrolmentCount;
                    }
                    return null;
                }
        );

        progRepoStub = (ProgrammeRepository) Proxy.newProxyInstance(
                ProgrammeRepository.class.getClassLoader(),
                new Class<?>[]{ProgrammeRepository.class},
                (proxy, method, args) -> null
        );

        adapterStub = new ApaarVerificationAdapter() {
            @Override
            public ApaarAdapterResult verifyApaar(String apaarId, String providedName, LocalDate providedDob) {
                if (customAdapterResult != null) return customAdapterResult;
                return ApaarAdapterResult.verified(apaarId, providedName, providedDob);
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        validationEngine = new ValidationEngine(studentRepoStub, progRepoStub, adapterStub);
    }

    private Student createValidStudent() {
        Student s = new Student(
                "202611223344",
                "Aarav Sharma",
                LocalDate.of(2005, 5, 15),
                "Male",
                testProg,
                testInst,
                2026,
                "REGULAR",
                "aarav.sharma@bmsce.ac.in",
                "9876543210",
                "PENDING",
                true
        );
        s.setId(100L);
        return s;
    }

    @Test
    @DisplayName("Rule 1: APAAR_MISSING flags blank or null APAAR ID")
    void testApaarMissing() {
        Student s = createValidStudent();
        s.setApaarId("   ");

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertEquals("ERROR", res.getStatus());
        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.APAAR_MISSING.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 2: APAAR_FORMAT flags invalid length or non-digits")
    void testApaarFormat() {
        Student s1 = createValidStudent();
        s1.setApaarId("12345"); // too short

        ValidationResult res1 = validationEngine.validateStudent(s1, new HashSet<>(), false);
        assertEquals("ERROR", res1.getStatus());
        assertTrue(res1.getIssues().stream().anyMatch(i -> ValidationRuleCode.APAAR_FORMAT.equals(i.getRuleCode())));

        Student s2 = createValidStudent();
        s2.setApaarId("1234ABCD5678"); // non-digits
        ValidationResult res2 = validationEngine.validateStudent(s2, new HashSet<>(), false);
        assertTrue(res2.getIssues().stream().anyMatch(i -> ValidationRuleCode.APAAR_FORMAT.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 3: DUPLICATE_IN_FILE flags same APAAR twice in same batch")
    void testDuplicateInFile() {
        Student s = createValidStudent();
        s.setApaarId("202611223344");

        Set<String> batchApaars = new HashSet<>();
        batchApaars.add("202611223344"); // already seen

        ValidationResult res = validationEngine.validateStudent(s, batchApaars, true);

        assertEquals("DUPLICATE", res.getStatus());
        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.DUPLICATE_IN_FILE.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 4: DUPLICATE_SAME_INSTITUTION flags already enrolled in same institution/year across programmes")
    void testDuplicateSameInstitution() {
        Student s = createValidStudent();

        Student existing = createValidStudent();
        existing.setId(999L);
        Programme otherProgramme = new Programme(testInst, "Electronics", "ECE", 2026, 120, 0);
        otherProgramme.setId(11L);
        existing.setProgramme(otherProgramme);
        sameInstResult = Collections.singletonList(existing);

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertEquals("DUPLICATE", res.getStatus());
        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.DUPLICATE_SAME_INSTITUTION.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 5: DUPLICATE_CROSS_INSTITUTION flags active enrolment at another institution")
    void testDuplicateCrossInstitution() {
        Student s = createValidStudent();

        Institution otherInst = new Institution("INST002", "COEP", "Maharashtra", "Pune", "ACTIVE");
        otherInst.setId(2L);
        Programme otherProg = new Programme(otherInst, "B.Tech CSE", "CSE", 2026, 120, 10);
        otherProg.setId(20L);

        Student otherStudent = new Student(
                "202611223344", "Aarav Sharma", LocalDate.of(2005, 5, 15), "Male",
                otherProg, otherInst, 2026, "REGULAR", "a@coep.ac.in", "9999999999", "VALID", true
        );
        otherStudent.setId(500L);
        crossInstResult = Collections.singletonList(otherStudent);

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertEquals("DUPLICATE", res.getStatus());
        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.DUPLICATE_CROSS_INSTITUTION.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 6: MANDATORY_FIELDS flags missing required fields")
    void testMandatoryFields() {
        Student s = createValidStudent();
        s.setName(null);
        s.setDob(null);

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.MANDATORY_FIELDS.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 7: INCONSISTENT_DATA flags unrealistic age or invalid category")
    void testInconsistentData() {
        Student s = createValidStudent();
        s.setDob(LocalDate.of(2020, 1, 1)); // 6 years old at admission 2026

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.INCONSISTENT_DATA.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 8: INTAKE_EXCEEDED flags actual enrolment exceeding approved intake + quota")
    void testIntakeExceeded() {
        Student s = createValidStudent();
        s.setId(null); // New student

        // Total permitted is 138 (120 approved + 18 quota). Set count to 138.
        currentProgEnrolmentCount = 138L;

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.INTAKE_EXCEEDED.equals(i.getRuleCode())));
    }

    @Test
    @DisplayName("Rule 9: APAAR_NOT_FOUND flags unknown IDs returned by adapter")
    void testApaarNotFound() {
        Student s = createValidStudent();
        s.setApaarId("999900001111");

        customAdapterResult = ApaarAdapterResult.notFound("999900001111");

        ValidationResult res = validationEngine.validateStudent(s, new HashSet<>(), false);

        assertTrue(res.getIssues().stream().anyMatch(i -> ValidationRuleCode.APAAR_NOT_FOUND.equals(i.getRuleCode())));
    }
}
