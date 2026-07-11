package akbar.rizki.expertsystem.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "frameworks", schema = "security_assessment")
public class Framework extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Column(name = "type", nullable = false)
    public String type; // FRONTEND | BACKEND | DATABASE | INFRA

    @Column(name = "language", nullable = false)
    public String language;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;
}
