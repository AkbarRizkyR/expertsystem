package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.CreateUserRequest;
import akbar.rizki.expertsystem.dto.UserResponse;
import akbar.rizki.expertsystem.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/users")
@RolesAllowed("ADMIN")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "List semua user (ADMIN)")
    public List<UserResponse> list() {
        return userService.list().stream().map(UserResponse::from).toList();
    }

    @POST
    @Operation(summary = "Buat user baru dengan password ter-hash BCrypt (ADMIN)")
    public Response create(@Valid CreateUserRequest request) {
        UserResponse response = UserResponse.from(userService.create(request));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
