package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.FrameworkResponse;
import akbar.rizki.expertsystem.entity.Framework;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/frameworks")
@RolesAllowed({"DEVELOPER", "SECURITY_REVIEWER", "ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
public class FrameworkResource {

    @GET
    @Operation(summary = "List framework (default hanya yang aktif)")
    public List<FrameworkResponse> list(
            @QueryParam("includeInactive") @DefaultValue("false") boolean includeInactive) {
        List<Framework> frameworks = includeInactive
                ? Framework.listAll()
                : Framework.list("isActive", true);
        return frameworks.stream().map(FrameworkResponse::from).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail 1 framework")
    public FrameworkResponse get(@PathParam("id") Integer id) {
        Framework framework = Framework.findById(id);
        if (framework == null) {
            throw new NotFoundException("Framework tidak ditemukan: " + id);
        }
        return FrameworkResponse.from(framework);
    }
}
