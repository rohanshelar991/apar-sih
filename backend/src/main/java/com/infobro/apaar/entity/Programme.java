package com.infobro.apaar.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "programmes", indexes = {
    @Index(name = "idx_prog_inst", columnList = "institution_id"),
    @Index(name = "idx_prog_code", columnList = "code")
})
public class Programme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "approved_intake", nullable = false)
    private Integer approvedIntake;

    @Column(name = "supernumerary_quota", nullable = false)
    private Integer supernumeraryQuota = 0;

    public Programme() {}

    public Programme(Institution institution, String name, String code, Integer academicYear, Integer approvedIntake, Integer supernumeraryQuota) {
        this.institution = institution;
        this.name = name;
        this.code = code;
        this.academicYear = academicYear;
        this.approvedIntake = approvedIntake;
        this.supernumeraryQuota = supernumeraryQuota;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }

    public Integer getApprovedIntake() { return approvedIntake; }
    public void setApprovedIntake(Integer approvedIntake) { this.approvedIntake = approvedIntake; }

    public Integer getSupernumeraryQuota() { return supernumeraryQuota; }
    public void setSupernumeraryQuota(Integer supernumeraryQuota) { this.supernumeraryQuota = supernumeraryQuota; }

    public Integer getTotalPermittedIntake() {
        return (approvedIntake != null ? approvedIntake : 0) + (supernumeraryQuota != null ? supernumeraryQuota : 0);
    }
}
