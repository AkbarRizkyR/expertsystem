package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "checklist_master", schema = "security_assessment")
public class ChecklistMaster extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "section_code", nullable = false)
    public String sectionCode;

    @Column(name = "section_name", nullable = false)
    public String sectionName;

    @Column(name = "sub_section_code", nullable = false)
    public String subSectionCode;

    @Column(name = "sub_section_name", nullable = false)
    public String subSectionName;

    @Column(name = "item_code", nullable = false, unique = true)
    public String itemCode;

    @Column(name = "item_description", nullable = false, columnDefinition = "TEXT")
    public String itemDescription;

    @Column(name = "asvs_level", nullable = false)
    public Short asvsLevel;

    @Column(name = "severity", nullable = false)
    public String severity;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;
}