package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scan_submissions", schema = "security_assessment")
public class ScanSubmission extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "application_checklist_item_id", nullable = false)
    public Long applicationChecklistItemId;

    @Column(name = "original_filename", nullable = false)
    public String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    public String storedFilename;

    @Column(name = "file_extension", nullable = false)
    public String fileExtension;

    @Column(name = "submitted_by", nullable = false)
    public Long submittedBy;

    @Column(name = "submitted_at", nullable = false)
    public OffsetDateTime submittedAt = OffsetDateTime.now();

    @Column(name = "scan_status", nullable = false)
    public String scanStatus = "PENDING"; // PENDING | SUCCESS | FAILED | TIMEOUT

    @Column(name = "scan_duration_ms")
    public Integer scanDurationMs;
}