package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "application_checklist_items", schema = "security_assessment")
public class ApplicationChecklistItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "application_id", nullable = false)
    public Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_master_id", nullable = false)
    public ChecklistMaster checklistMaster;

    @Column(name = "status", nullable = false)
    public String status = "NA"; // NA | VALID | INVALID

    @Column(name = "keterangan", columnDefinition = "TEXT")
    public String keterangan;

    @Column(name = "updated_by")
    public Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt = OffsetDateTime.now();
}