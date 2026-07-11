package akbar.rizki.expertsystem.service;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    /** Ambil id user dari klaim JWT (subject). */
    public Long id() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new NotAuthorizedException("Token tidak valid atau tidak ada", "Bearer");
        }
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            throw new NotAuthorizedException("Token tidak valid", "Bearer");
        }
    }

    public boolean hasRole(String role) {
        return identity.hasRole(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
