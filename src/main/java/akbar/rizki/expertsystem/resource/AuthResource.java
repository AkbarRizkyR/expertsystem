package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.LoginRequest;
import akbar.rizki.expertsystem.dto.LoginResponse;
import akbar.rizki.expertsystem.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Login dengan email & password, mengembalikan JWT")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.authenticate(request);
    }
}
