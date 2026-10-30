package com.infobro.apaar;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.Institution;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.service.IntakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntakeMonitoringTest {

    private Institution testInst;
    private Programme testProg;

    @BeforeEach
    void setUp() {
        testInst = new Institution("INST001", "BMS College of Engineering", "Karnataka", "Bengaluru", "ACTIVE");
        testInst.setId(1L);

        // Approved 100, Quota 10 -> Total Permitted = 110
        testProg = new Programme(testInst, "B.Tech Mechanical Engineering", "ME", 2026, 100, 10);
        testProg.setId(5L);
    }

    private ProgrammeRepository createProgrammeRepo(List<Programme> programmes) {
        return (ProgrammeRepository) Proxy.newProxyInstance(
                ProgrammeRepository.class.getClassLoader(),
                new Class<?>[]{ProgrammeRepository.class},
                (proxy, method, args) -> {
                    if ("findByInstitutionId".equals(method.getName())) {
                        return programmes;
                    }
                    if ("findAll".equals(method.getName())) {
                        return programmes;
                    }
                    return null;
                }
        );
    }

    private StudentRepository createStudentRepo(long studentCount) {
        return (StudentRepository) Proxy.newProxyInstance(
                StudentRepository.class.getClassLoader(),
                new Class<?>[]{StudentRepository.class},
                (proxy, method, args) -> {
                    if ("countByProgrammeId".equals(method.getName())) {
                        return studentCount;
                    }
                    return 0L;
                }
        );
    }

    @Test
    @DisplayName("Intake Status returns 'OK' when utilization < 90%")
    void testIntakeStatusOk() {
        ProgrammeRepository progRepo = createProgrammeRepo(Collections.singletonList(testProg));
        StudentRepository studentRepo = createStudentRepo(85L); // 85 / 110 = 77.3% (< 90%)

        IntakeService service = new IntakeService(null, progRepo, studentRepo, null, null);
        List<DTOs.ProgrammeIntakeDto> list = service.getProgrammeIntakes(1L, null);

        assertEquals(1, list.size());
        assertEquals("OK", list.get(0).getStatus());
        assertEquals(77.3, list.get(0).getUtilizationPercent());
    }

    @Test
    @DisplayName("Intake Status returns 'WARNING' when utilization between 90% and 100%")
    void testIntakeStatusWarning() {
        ProgrammeRepository progRepo = createProgrammeRepo(Collections.singletonList(testProg));
        StudentRepository studentRepo = createStudentRepo(105L); // 105 / 110 = 95.5% (90-100%)

        IntakeService service = new IntakeService(null, progRepo, studentRepo, null, null);
        List<DTOs.ProgrammeIntakeDto> list = service.getProgrammeIntakes(1L, null);

        assertEquals("WARNING", list.get(0).getStatus());
        assertEquals(95.5, list.get(0).getUtilizationPercent());
    }

    @Test
    @DisplayName("Intake Status returns 'EXCEEDED' when utilization > 100%")
    void testIntakeStatusExceeded() {
        ProgrammeRepository progRepo = createProgrammeRepo(Collections.singletonList(testProg));
        StudentRepository studentRepo = createStudentRepo(115L); // 115 / 110 = 104.5% (> 100%)

        IntakeService service = new IntakeService(null, progRepo, studentRepo, null, null);
        List<DTOs.ProgrammeIntakeDto> list = service.getProgrammeIntakes(1L, null);

        assertEquals("EXCEEDED", list.get(0).getStatus());
        assertEquals(104.5, list.get(0).getUtilizationPercent());
    }
}
