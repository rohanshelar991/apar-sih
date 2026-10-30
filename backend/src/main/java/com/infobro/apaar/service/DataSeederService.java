package com.infobro.apaar.service;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Profile("(h2 & !postgres) | dev")
@ConditionalOnProperty(name = "apaar.seed-demo-data", havingValue = "true")
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final InstitutionRepository institutionRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DataSeederService(InstitutionRepository institutionRepository,
                             ProgrammeRepository programmeRepository,
                             StudentRepository studentRepository,
                             UserAccountRepository userAccountRepository,
                             ValidationIssueRepository validationIssueRepository,
                             PasswordEncoder passwordEncoder,
                             AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.programmeRepository = programmeRepository;
        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.validationIssueRepository = validationIssueRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (institutionRepository.count() > 0) {
            log.info("Database already seeded with institutions. Skipping initial seed.");
            return;
        }

        log.info("Starting synthetic database seeding for SIH 2026 APAAR-Connect prototype...");

        // 1. Seed 10 Institutions
        List<Institution> institutions = new ArrayList<>();
        institutions.add(new Institution("INST001", "BMS College of Engineering", "Karnataka", "Bengaluru", "ACTIVE"));
        institutions.add(new Institution("INST002", "College of Engineering Pune (COEP)", "Maharashtra", "Pune", "ACTIVE"));
        institutions.add(new Institution("INST003", "Veermata Jijabai Technological Institute (VJTI)", "Maharashtra", "Mumbai", "ACTIVE"));
        institutions.add(new Institution("INST004", "Delhi Technological University (DTU)", "Delhi", "New Delhi", "ACTIVE"));
        institutions.add(new Institution("INST005", "PSG College of Technology", "Tamil Nadu", "Coimbatore", "ACTIVE"));
        institutions.add(new Institution("INST006", "Jadavpur University Faculty of Engg", "West Bengal", "Kolkata", "ACTIVE"));
        institutions.add(new Institution("INST007", "R.V. College of Engineering", "Karnataka", "Bengaluru", "ACTIVE"));
        institutions.add(new Institution("INST008", "National Institute of Technology Tiruchirappalli", "Tamil Nadu", "Tiruchirappalli", "ACTIVE"));
        institutions.add(new Institution("INST009", "College of Engineering Guindy (Anna Univ)", "Tamil Nadu", "Chennai", "ACTIVE"));
        institutions.add(new Institution("INST010", "Thapar Institute of Engineering and Technology", "Punjab", "Patiala", "ACTIVE"));
        institutions = institutionRepository.saveAll(institutions);

        // 2. Seed Programmes for each Institution
        List<Programme> programmes = new ArrayList<>();
        Programme bmsceCse = null;
        Programme coepCse = null;

        for (Institution inst : institutions) {
            Programme cse = new Programme(inst, "B.Tech Computer Science & Engineering", "CSE", 2026, 120, 18);
            Programme ece = new Programme(inst, "B.Tech Electronics & Communication", "ECE", 2026, 120, 15);
            Programme me = new Programme(inst, "B.Tech Mechanical Engineering", "ME", 2026, 90, 10);
            Programme ce = new Programme(inst, "B.Tech Civil Engineering", "CE", 2026, 60, 8);
            Programme mba = new Programme(inst, "Master of Business Administration", "MBA", 2026, 60, 6);

            programmes.add(cse);
            programmes.add(ece);
            programmes.add(me);
            programmes.add(ce);
            programmes.add(mba);

            if ("INST001".equals(inst.getCode())) bmsceCse = cse;
            if ("INST002".equals(inst.getCode())) coepCse = cse;
        }
        programmes = programmeRepository.saveAll(programmes);

        // 3. Seed Demo User Accounts
        String passHash = passwordEncoder.encode("Admin@123");
        String regHash = passwordEncoder.encode("Regulator@123");
        String verHash = passwordEncoder.encode("Verifier@123");

        UserAccount instAdmin = new UserAccount(
                "admin@bmsce.ac.in",
                passHash,
                "Dr. S. K. Ramesh (Principal / Admin)",
                "ROLE_INSTITUTION_ADMIN",
                institutions.get(0)
        );

        UserAccount aicteRegulator = new UserAccount(
                "regulator@aicte-india.org",
                regHash,
                "Dr. Amit Banerjee (AICTE Regional Advisor)",
                "ROLE_AICTE_REGULATOR",
                null
        );

        UserAccount verifier = new UserAccount(
                "verifier@tcs.com",
                verHash,
                "Rajesh Kulkarni (Lead Talent Verification, TCS)",
                "ROLE_AUTHORIZED_VERIFIER",
                null
        );

        userAccountRepository.saveAll(Arrays.asList(instAdmin, aicteRegulator, verifier));

        // 4. Seed ~1,500 Realistic Students
        log.info("Generating synthetic student records...");
        List<Student> students = new ArrayList<>();
        Random rng = new Random(42);

        String[] firstNames = {
            "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
            "Shaurya", "Atharva", "Advik", "Pranav", "Kabir", "Ananya", "Diya", "Isha", "Saanvi", "Myra",
            "Aadhya", "Kiara", "Pari", "Navya", "Riya", "Aditi", "Tara", "Tanvi", "Vaidehi", "Anika",
            "Meera", "Kavya", "Bhavya", "Divya", "Siddharth", "Rohan", "Rahul", "Varun", "Karan", "Nikhil"
        };
        String[] lastNames = {
            "Sharma", "Verma", "Gupta", "Malhotra", "Bhat", "Patel", "Shah", "Mehta", "Deshmukh", "Kulkarni",
            "Joshi", "Iyer", "Nair", "Menon", "Reddy", "Rao", "Choudhury", "Das", "Banerjee", "Chatterjee",
            "Singh", "Kaur", "Kumar", "Mishra", "Pandey", "Agarwal", "Bansal", "Garg", "Jain", "Bose"
        };
        String[] categories = {"REGULAR", "REGULAR", "REGULAR", "SUPERNUMERARY", "NRI", "OCI"};
        String[] genders = {"Male", "Female", "Male", "Female", "Other"};

        long apaarCounter = 202610000000L;

        // Reserve specific APAAR for cross-institution duplicate demo scenario:
        // Student in COEP INST002: APAAR "202488339101"
        Student crossInstStudent = new Student(
                "202488339101",
                "Kunal Deshmukh",
                LocalDate.of(2005, 5, 14),
                "Male",
                coepCse != null ? coepCse : programmes.get(5),
                institutions.get(1), // COEP INST002
                2026,
                "REGULAR",
                "kunal.deshmukh@coep.ac.in",
                "9876543210",
                "VALID",
                true
        );
        students.add(crossInstStudent);

        // Populate students across all institutions
        for (Programme prog : programmes) {
            // For BMSCE CSE, seed 115 students so that when INST001 uploads ~35 in the demo file, it pushes past 138 total permitted!
            int targetCount;
            if (prog.equals(bmsceCse)) {
                targetCount = 115;
            } else if ("CSE".equals(prog.getCode())) {
                targetCount = 95 + rng.nextInt(20);
            } else if ("ECE".equals(prog.getCode())) {
                targetCount = 80 + rng.nextInt(25);
            } else if ("ME".equals(prog.getCode())) {
                targetCount = 55 + rng.nextInt(20);
            } else if ("CE".equals(prog.getCode())) {
                targetCount = 40 + rng.nextInt(15);
            } else {
                targetCount = 45 + rng.nextInt(12);
            }

            for (int i = 0; i < targetCount; i++) {
                apaarCounter++;
                String fName = firstNames[rng.nextInt(firstNames.length)];
                String lName = lastNames[rng.nextInt(lastNames.length)];
                String fullName = fName + " " + lName;
                String gender = genders[rng.nextInt(genders.length)];
                String cat = categories[rng.nextInt(categories.length)];

                int birthYear = 2004 + rng.nextInt(3);
                int birthMonth = 1 + rng.nextInt(12);
                int birthDay = 1 + rng.nextInt(28);
                LocalDate dob = LocalDate.of(birthYear, birthMonth, birthDay);

                int admYear = (rng.nextDouble() < 0.8) ? 2026 : (rng.nextDouble() < 0.6 ? 2025 : 2024);

                String email = fName.toLowerCase() + "." + lName.toLowerCase() + (10 + rng.nextInt(90)) + "@" +
                        prog.getInstitution().getCode().toLowerCase() + ".edu.in";
                String phone = "9" + (100000000L + rng.nextInt(900000000));

                String status = (rng.nextDouble() < 0.94) ? "VALID" : "PENDING";

                Student s = new Student(
                        String.valueOf(apaarCounter),
                        fullName,
                        dob,
                        gender,
                        prog,
                        prog.getInstitution(),
                        admYear,
                        cat,
                        email,
                        phone,
                        status,
                        true
                );
                students.add(s);
            }
        }

        studentRepository.saveAll(students);
        log.info("Successfully seeded {} synthetic student records across 10 institutions.", students.size());

        // 5. Seed a few initial audit logs and validation anomalies for Regulator Anomaly Center
        auditService.log(
                "SYSTEM",
                "ROLE_SYSTEM",
                "SYSTEM_INITIALIZATION",
                "Platform",
                "AICTE-01",
                "127.0.0.1",
                "Seeded base institutional data with 10 AICTE-accredited universities and synthetic student registry."
        );

        auditService.log(
                "admin@bmsce.ac.in",
                "ROLE_INSTITUTION_ADMIN",
                "LOGIN_SUCCESS",
                "UserAccount",
                "1",
                "192.168.1.45",
                "Institutional login authenticated for BMS College of Engineering"
        );

        log.info("Data seeding completed successfully!");
    }
}
