package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.KnowledgeRuleRequest;
import akbar.rizki.expertsystem.dto.KnowledgeRuleResponse;
import akbar.rizki.expertsystem.service.ReviewService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/knowledge-rules")
@RolesAllowed({"SECURITY_REVIEWER", "ADMIN"})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KnowledgeRuleResource {

    @Inject
    ReviewService reviewService;

    @GET
    @Operation(summary = "List semua knowledge rule")
    public List<KnowledgeRuleResponse> list() {
        return reviewService.listRules().stream().map(KnowledgeRuleResponse::from).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail 1 knowledge rule")
    public KnowledgeRuleResponse get(@PathParam("id") Long id) {
        return KnowledgeRuleResponse.from(reviewService.getRule(id));
    }

    @POST
    @Operation(summary = "Tambah knowledge rule baru")
    public Response create(@Valid KnowledgeRuleRequest request) {
        KnowledgeRuleResponse response = KnowledgeRuleResponse.from(reviewService.createRule(request));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/{id}/update")
    @Operation(summary = "Update knowledge rule")
    public KnowledgeRuleResponse update(@PathParam("id") Long id, @Valid KnowledgeRuleRequest request) {
        return KnowledgeRuleResponse.from(reviewService.updateRule(id, request));
    }

    @POST
    @Path("/{id}/delete")
    @Operation(summary = "Hapus knowledge rule")
    public Response delete(@PathParam("id") Long id) {
        reviewService.deleteRule(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/from-unmatched/{queueId}")
    @Operation(summary = "Buat knowledge rule dari item unmatched_queue lalu tandai queue-nya reviewed")
    public Response createFromUnmatched(@PathParam("queueId") Long queueId,
                                        @Valid KnowledgeRuleRequest request) {
        KnowledgeRuleResponse response = KnowledgeRuleResponse.from(
                reviewService.createRuleFromUnmatched(queueId, request));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
