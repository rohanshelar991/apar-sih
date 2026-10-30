package com.infobro.apaar.validation;

import com.infobro.apaar.adapter.ApaarAdapterResult;
import com.infobro.apaar.adapter.ApaarVerificationAdapter;
import com.infobro.apaar.entity.Institution;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.entity.Student;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
public class ValidationEngine {

    private final StudentRepository studentRepository;
    private final ProgrammeRepository programmeRepository;
    private final ApaarVerificationAdapter apaarVerificationAdapter;

    private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList(
        "REGULAR", "SUPERNUMERARY", "NRI", "OCI", "FOREIGN_NATIONAL"
    ));

    public ValidationEngine(StudentRepository studentRepository,
                            ProgrammeRepository programmeRepository,
                            ApaarVerificationAdapter apaarVerificationAdapter) {
        this.studentRepository = studentRepository;
        this.programmeRepository = programmeRepository;
        this.apaarVerificationAdapter = apaarVerificationAdapter;
    }

    /**
     * Normalizes APAAR ID by stripping whitespace and dashes
     */
    public String normalizeApaarId(String apaarId) {
        if (apaarId == null) return null;
        return apaarId.replaceAll("[\\s-]+", "").trim();
    }

    /**
     * Validate a single student record during creation/update or within a batch context.
     *
     * @param student The student object to validate
     * @param currentBatchApaars Set of APAAR IDs already encountered in the current upload batch (for duplicate in file check)
     * @param isBatchContext Whether this validation is part of a bulk CSV upload
     * @return ValidationResult containing status and detected issues
     */
    public ValidationResult validateStudent(Student student, Set<String> currentBatchApaars, boolean isBatchContext) {
        ValidationResult result = new ValidationResult();

        // 1. Mandatory Fields Check
        List<String> missingFields = new ArrayList<>();
        if (student.getName() == null || student.getName().trim().isEmpty()) missingFields.add("Name");
        if (student.getDob() == null) missingFields.add("Date of Birth");
        if (student.getProgramme() == null) missingFields.add("Programme");
        if (student.getCategory() == null || student.getCategory().trim().isEmpty()) missingFields.add("Category");
        if (student.getAdmissionYear() == null) missingFields.add("Admission Year");

        if (!missingFields.isEmpty()) {
            result.addIssue(
                ValidationRuleCode.MANDATORY_FIELDS,
                "ERROR",
                "Missing mandatory field(s): " + String.join(", ", missingFields),
                "Provide all mandatory fields: Name, Date of Birth, Programme, Category, and Admission Year."
            );
        }

        // 2. APAAR ID Checks
        String rawApaar = student.getApaarId();
        if (rawApaar == null || rawApaar.trim().isEmpty()) {
            result.addIssue(
                ValidationRuleCode.APAAR_MISSING,
                "ERROR",
                "APAAR ID is missing.",
                "Provide the 12-digit APAAR ID assigned to the student."
            );
            return result; // Cannot proceed with APAAR-based validations
        }

        String normalizedApaar = normalizeApaarId(rawApaar);
        student.setApaarId(normalizedApaar);

        if (!normalizedApaar.matches("^\\d{12}$")) {
            result.addIssue(
                ValidationRuleCode.APAAR_FORMAT,
                "ERROR",
                "Invalid APAAR ID format: '" + rawApaar + "'. APAAR ID must be exactly 12 numeric digits.",
                "Verify student APAAR/ABC card and enter exactly 12 digits (e.g. 123456789012)."
            );
            return result;
        }

        // 3. Duplicate in File Check (for bulk upload)
        if (isBatchContext && currentBatchApaars != null) {
            if (currentBatchApaars.contains(normalizedApaar)) {
                result.addIssue(
                    ValidationRuleCode.DUPLICATE_IN_FILE,
                    "ERROR",
                    "Duplicate APAAR ID within this upload batch: " + normalizedApaar,
                    "Remove redundant duplicate entry from your upload spreadsheet."
                );
            } else {
                currentBatchApaars.add(normalizedApaar);
            }
        }

        // 4. Inconsistent Data Checks (Age, Admission Year, Category, conflicting existing data)
        if (student.getDob() != null && student.getAdmissionYear() != null) {
            LocalDate admissionDate = LocalDate.of(student.getAdmissionYear(), 8, 1);
            int ageAtAdmission = Period.between(student.getDob(), admissionDate).getYears();
            if (ageAtAdmission < 15 || ageAtAdmission > 75) {
                result.addIssue(
                    ValidationRuleCode.INCONSISTENT_DATA,
                    "ERROR",
                    "Unrealistic age (" + ageAtAdmission + " years) at admission year " + student.getAdmissionYear() +
                    " based on DOB " + student.getDob(),
                    "Correct student Date of Birth or Admission Year."
                );
            }
        }

        if (student.getAdmissionYear() != null && (student.getAdmissionYear() < 2015 || student.getAdmissionYear() > 2030)) {
            result.addIssue(
                ValidationRuleCode.INCONSISTENT_DATA,
                "ERROR",
                "Admission year " + student.getAdmissionYear() + " is outside permitted AICTE reporting window (2015-2030).",
                "Check and correct the admission year."
            );
        }

        if (student.getCategory() != null) {
            String normCategory = student.getCategory().trim().toUpperCase().replace(" ", "_");
            if (!VALID_CATEGORIES.contains(normCategory)) {
                result.addIssue(
                    ValidationRuleCode.INCONSISTENT_DATA,
                    "ERROR",
                    "Invalid student category '" + student.getCategory() + "'. Permitted values: Regular, Supernumerary, NRI, OCI, Foreign National.",
                    "Select a valid AICTE category."
                );
            } else {
                student.setCategory(normCategory);
            }
        }

        // Check if student exists with same APAAR in database for conflicting name or DOB
        List<Student> existingWithApaar = studentRepository.findByApaarId(normalizedApaar);
        for (Student existing : existingWithApaar) {
            if (student.getId() != null && student.getId().equals(existing.getId())) {
                continue; // Skip self
            }
            if (student.getName() != null && !student.getName().equalsIgnoreCase(existing.getName())) {
                result.addIssue(
                    ValidationRuleCode.INCONSISTENT_DATA,
                    "ERROR",
                    "Conflicting name for APAAR ID " + normalizedApaar + ": '" + student.getName() +
                    "' conflicts with existing record '" + existing.getName() + "'.",
                    "Verify student identity documents or rectify typographical discrepancies."
                );
            }
            if (student.getDob() != null && !student.getDob().equals(existing.getDob())) {
                result.addIssue(
                    ValidationRuleCode.INCONSISTENT_DATA,
                    "ERROR",
                    "Conflicting DOB for APAAR ID " + normalizedApaar + ": " + student.getDob() +
                    " conflicts with existing record " + existing.getDob() + ".",
                    "Verify student birth certificate or ABC registry record."
                );
            }
        }

        // 5. Duplicate in Same Institution Check
        if (student.getInstitution() != null && student.getAdmissionYear() != null) {
            List<Student> sameInstDuplicates = studentRepository.findByApaarIdAndInstitutionIdAndAdmissionYearAndVerificationStatusIn(
                normalizedApaar,
                student.getInstitution().getId(),
                student.getAdmissionYear(),
                Arrays.asList("VALID", "PENDING", "VERIFIED")
            );
            boolean isSameInstDup = sameInstDuplicates.stream()
                .anyMatch(s -> student.getId() == null || !s.getId().equals(student.getId()));
            if (isSameInstDup) {
                result.addIssue(
                    ValidationRuleCode.DUPLICATE_SAME_INSTITUTION,
                    "ERROR",
                    "Student with APAAR ID " + normalizedApaar + " is already enrolled in " +
                    student.getInstitution().getName() + " for academic year " + student.getAdmissionYear() + ".",
                    "Update existing student record instead of submitting duplicate enrolment."
                );
            }
        }

        // 6. Cross-Institution Duplicate Check (Flag for review, do not auto-delete)
        if (student.getInstitution() != null && student.getAdmissionYear() != null) {
            List<Student> yearEnrolments = studentRepository.findByApaarIdAndAdmissionYear(
                normalizedApaar,
                student.getAdmissionYear()
            );
            for (Student other : yearEnrolments) {
                if (student.getId() != null && student.getId().equals(other.getId())) {
                    continue;
                }
                if (!other.getInstitution().getId().equals(student.getInstitution().getId())) {
                    result.addIssue(
                        ValidationRuleCode.DUPLICATE_CROSS_INSTITUTION,
                        "WARNING",
                        "Cross-institution duplicate: APAAR ID " + normalizedApaar +
                        " is already enrolled at " + other.getInstitution().getName() +
                        " (" + other.getInstitution().getCode() + ") in " + other.getProgramme().getName() +
                        " for academic year " + student.getAdmissionYear() + ".",
                        "Flagged for AICTE Regulator review. Contact institution or student to resolve dual-enrolment conflict."
                    );
                    break;
                }
            }
        }

        // 7. Simulated APAAR Verification Adapter Check
        if (apaarVerificationAdapter.isAvailable()) {
            ApaarAdapterResult adapterResult = apaarVerificationAdapter.verifyApaar(
                normalizedApaar,
                student.getName(),
                student.getDob()
            );
            if (!adapterResult.isSuccess()) {
                if (adapterResult.getStatus() == ApaarAdapterResult.Status.NOT_FOUND) {
                    result.addIssue(
                        ValidationRuleCode.APAAR_NOT_FOUND,
                        "ERROR",
                        "APAAR ID " + normalizedApaar + " was not found in the simulated ABC registry.",
                        "Advise student to generate APAAR ID via DigiLocker / ABC portal prior to registration."
                    );
                } else if (adapterResult.getStatus() == ApaarAdapterResult.Status.NAME_MISMATCH) {
                    result.addIssue(
                        ValidationRuleCode.INCONSISTENT_DATA,
                        "ERROR",
                        adapterResult.getMessage(),
                        "Match student name exactly with official APAAR / Aadhaar credentials."
                    );
                }
            }
        }

        // 8. Intake Exceeded Check
        if (student.getProgramme() != null) {
            Programme prog = student.getProgramme();
            long currentCount = studentRepository.countByProgrammeIdAndVerificationStatusIn(
                prog.getId(),
                Arrays.asList("VALID", "PENDING", "VERIFIED")
            );
            // If validating a new student, add 1 to projected count
            long projectedCount = (student.getId() == null) ? currentCount + 1 : currentCount;
            int totalPermitted = prog.getTotalPermittedIntake();

            if (projectedCount > totalPermitted) {
                result.addIssue(
                    ValidationRuleCode.INTAKE_EXCEEDED,
                    "WARNING",
                    "Intake exceeded for programme '" + prog.getName() + "': Total permitted is " +
                    totalPermitted + " (Approved: " + prog.getApprovedIntake() + ", Supernumerary: " +
                    prog.getSupernumeraryQuota() + "), but enrolment reaches " + projectedCount + ".",
                    "Obtain AICTE approval for additional intake or reallocate to another programme/branch."
                );
            }
        }

        return result;
    }
}
