package com.infobro.apaar;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class StudentRepositoryTest {
    @Autowired InstitutionRepository institutions;
    @Autowired ProgrammeRepository programmes;
    @Autowired StudentRepository students;

    private Student enrol(Institution institution, Programme programme, int year, String status) {
        return students.saveAndFlush(new Student("202611223344", "Student", LocalDate.of(2005, 1, 1),
            "Other", programme, institution, year, "REGULAR", null, null, status, true));
    }

    @Test
    void crossInstitutionReportExcludesOtherYears() {
        Institution a = institutions.save(new Institution("A", "A", "State", "City", "ACTIVE"));
        Institution b = institutions.save(new Institution("B", "B", "State", "City", "ACTIVE"));
        Programme pa = programmes.save(new Programme(a, "CS", "CS", 2026, 100, 0));
        Programme pb = programmes.save(new Programme(b, "CS", "CS", 2026, 100, 0));
        Student first = enrol(a, pa, 2026, "VALID");
        Student second = enrol(b, pb, 2026, "VALID");
        enrol(a, pa, 2025, "VALID");
        assertThat(students.findCrossInstitutionDuplicates(2026))
            .extracting(Student::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void sameInstitutionLookupCountsOnlyCountedStatusesAcrossProgrammes() {
        Institution a = institutions.save(new Institution("A", "A", "State", "City", "ACTIVE"));
        Programme cs = programmes.save(new Programme(a, "CS", "CS", 2026, 100, 0));
        Programme ec = programmes.save(new Programme(a, "EC", "EC", 2026, 100, 0));
        Student valid = enrol(a, cs, 2026, "VALID");
        Student pending = enrol(a, ec, 2026, "PENDING");
        Student verified = enrol(a, ec, 2026, "VERIFIED");
        enrol(a, ec, 2026, "ERROR");
        enrol(a, ec, 2026, "DUPLICATE");
        enrol(a, ec, 2025, "VALID");
        assertThat(students.findByApaarIdAndInstitutionIdAndAdmissionYearAndVerificationStatusIn(
            "202611223344", a.getId(), 2026, List.of("VALID", "PENDING", "VERIFIED")))
            .extracting(Student::getId).containsExactlyInAnyOrder(valid.getId(), pending.getId(), verified.getId());
    }

    @Test
    void optionalDirectoryFiltersAndSearchRetainPaginationAndExportBehavior() {
        Institution a = institutions.save(new Institution("A", "A", "State", "City", "ACTIVE"));
        Programme cs = programmes.save(new Programme(a, "CS", "CS", 2026, 100, 0));
        Student current = enrol(a, cs, 2026, "VALID");
        enrol(a, cs, 2025, "ERROR");
        assertThat(students.findWithFilters(null, null, null, null, null, null, PageRequest.of(0, 1)).getTotalElements()).isEqualTo(2);
        assertThat(students.findWithFilters(a.getId(), cs.getId(), "REGULAR", "VALID", 2026, "STUDENT", PageRequest.of(0, 10)).getContent())
            .extracting(Student::getId).containsExactly(current.getId());
        assertThat(students.findWithFilters(null, null, null, null, null, "11223344", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(students.findAllWithFilters(a.getId(), cs.getId(), "REGULAR", "VALID", 2026))
            .extracting(Student::getId).containsExactly(current.getId());
    }
}
