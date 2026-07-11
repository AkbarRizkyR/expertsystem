package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.UnmatchedQueueResponse;
import akbar.rizki.expertsystem.service.ReviewService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/unmatched-queue")
@RolesAllowed({"SECURITY_REVIEWER", "ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
public class UnmatchedQueueResource {

    @Inject
    ReviewService reviewService;

    @GET
    @Operation(summary = "List item unmatched_queue (default hanya yang belum direview)")
    public List<UnmatchedQueueResponse> list(
            @QueryParam("includeReviewed") @DefaultValue("false") boolean includeReviewed) {
        return reviewService.listUnmatched(includeReviewed);
    }

    @POST
    @Path("/{id}/review")
    @Operation(summary = "Tandai 1 item unmatched_queue sebagai sudah direview")
    public Response markReviewed(@PathParam("id") Long id) {
        reviewService.markReviewed(id);
        return Response.noContent().build();
    }
}
