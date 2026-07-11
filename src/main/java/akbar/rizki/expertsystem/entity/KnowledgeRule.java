package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "knowledge_rules", schema = "security_assessment")
public class KnowledgeRule extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_master_id", nullable = false)
    public ChecklistMaster checklistMaster;

    @Column(name = "framework_id", nullable = false)
    public Integer frameworkId;

    @Column(name = "semgrep_check_id", nullable = false)
    public String semgrepCheckId;

    @Column(name = "recommendation_text", nullable = false, columnDefinition = "TEXT")
    public String recommendationText;

    @Column(name = "severity_source")
    public String severitySource;

    @Column(name = "reference_url")
    public String referenceUrl;

    /** Cari rule berdasarkan check_id Semgrep + framework yang dipakai aplikasi. */
    public static KnowledgeRule findByCheckIdAndFramework(String semgrepCheckId, Integer frameworkId) {
        return find("semgrepCheckId = ?1 and frameworkId = ?2", semgrepCheckId, frameworkId).firstResult();
    }
}