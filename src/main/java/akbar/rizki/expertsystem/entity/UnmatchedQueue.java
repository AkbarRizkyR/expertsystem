package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "unmatched_queue", schema = "security_assessment")
public class UnmatchedQueue extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "scan_result_id", nullable = false, unique = true)
    public Long scanResultId;

    @Column(name = "reviewed", nullable = false)
    public boolean reviewed = false;

    @Column(name = "reviewed_by")
    public Long reviewedBy;
}