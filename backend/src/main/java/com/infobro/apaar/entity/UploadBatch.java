package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload_batches", indexes = {
    @Index(name = "idx_batch_inst", columnList = "institution_id")
})
public class UploadBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Column(name = "valid_rows", nullable = false)
    private Integer validRows = 0;

    @Column(name = "error_rows", nullable = false)
    private Integer errorRows = 0;

    @Column(name = "duplicate_rows", nullable = false)
    private Integer duplicateRows = 0;

    @Column(name = "uploaded_by", nullable = false, length = 120)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(nullable = false, length = 24)
    private String status = "COMPLETED";

    public UploadBatch() {}

    public UploadBatch(Institution institution, String filename, Integer totalRows, Integer validRows,
                       Integer errorRows, Integer duplicateRows, String uploadedBy, String status) {
        this.institution = institution;
        this.filename = filename;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.errorRows = errorRows;
        this.duplicateRows = duplicateRows;
        this.uploadedBy = uploadedBy;
        this.status = status;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getValidRows() { return validRows; }
    public void setValidRows(Integer validRows) { this.validRows = validRows; }

    public Integer getErrorRows() { return errorRows; }
    public void setErrorRows(Integer errorRows) { this.errorRows = errorRows; }

    public Integer getDuplicateRows() { return duplicateRows; }
    public void setDuplicateRows(Integer duplicateRows) { this.duplicateRows = duplicateRows; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
