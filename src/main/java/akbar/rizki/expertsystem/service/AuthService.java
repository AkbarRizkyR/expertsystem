package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.dto.LoginRequest;
import akbar.rizki.expertsystem.dto.LoginResponse;
import akbar.rizki.expertsystem.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.duration.seconds", defaultValue = "28800")
    long durationSeconds;

    public LoginResponse authenticate(LoginRequest request) {
        User user = User.findByEmail(request.email);

        // pesan error sengaja disamakan (user tidak ada / password salah) supaya tidak
        // membocorkan email mana yang terdaftar (user enumeration)
        if (user == null || !BcryptUtil.matches(request.password, user.passwordHash)) {
            throw new NotAuthorizedException("Email atau password salah", "Bearer");
        }

        String roleName = user.role.name;

        String token = Jwt.issuer(issuer)
                .upn(user.email)
                .subject(String.valueOf(user.id))
                .groups(Set.of(roleName))
                .claim("uid", user.id)
                .claim("name", user.name)
                .expiresIn(Duration.ofSeconds(durationSeconds))
                .sign();

        return new LoginResponse(token, user.id, user.name, roleName);
    }
}
