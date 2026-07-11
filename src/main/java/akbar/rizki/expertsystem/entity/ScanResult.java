package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scan_results", schema = "security_assessment")
public class ScanResult extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "submission_id", nullable = false)
    public Long submissionId;

    @Column(name = "semgrep_check_id", nullable = false)
    public String semgrepCheckId;

    @Column(name = "matched_line")
    public Integer matchedLine;

    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    @Column(name = "severity_raw")
    public String severityRaw;

    @Column(name = "matched_rule_id")
    public Long matchedRuleId; // null = tidak ketemu di knowledge_rules (masuk unmatched_queue)

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt = OffsetDateTime.now();
}