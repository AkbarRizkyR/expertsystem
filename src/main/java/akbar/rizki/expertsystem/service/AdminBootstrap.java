package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.entity.Role;
import akbar.rizki.expertsystem.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Membuat 1 user ADMIN awal saat startup HANYA jika tabel users masih kosong,
 * supaya bisa login pertama kali. Setelah ada user, bootstrap ini tidak melakukan apa-apa.
 */
public class AdminBootstrap {

    private static final Logger LOG = Logger.getLogger(AdminBootstrap.class);

    @ConfigProperty(name = "app.bootstrap-admin.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "app.bootstrap-admin.email", defaultValue = "admin@expertsystem.local")
    String email;

    @ConfigProperty(name = "app.bootstrap-admin.password", defaultValue = "admin12345")
    String password;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!enabled) {
            return;
        }
        if (User.count() > 0) {
            return;
        }

        Role adminRole = Role.find("name", "ADMIN").firstResult();
        if (adminRole == null) {
            LOG.warn("Bootstrap admin dilewati: role ADMIN belum ada di tabel roles");
            return;
        }

        User admin = new User();
        admin.name = "Administrator";
        admin.email = email;
        admin.passwordHash = BcryptUtil.bcryptHash(password);
        admin.role = adminRole;
        admin.persist();

        LOG.warnf("User ADMIN awal dibuat: email=%s (SEGERA ganti password default ini!)", email);
    }
}
