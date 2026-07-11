package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.ApplicationRequest;
import akbar.rizki.expertsystem.dto.ApplicationResponse;
import akbar.rizki.expertsystem.dto.ChecklistItemResponse;
import akbar.rizki.expertsystem.dto.ChecklistItemUpdateRequest;
import akbar.rizki.expertsystem.service.ApplicationService;
import akbar.rizki.expertsystem.service.ChecklistService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/applications")
@RolesAllowed({"DEVELOPER", "ADMIN"})
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    ApplicationService applicationService;

    @Inject
    ChecklistService checklistService;

    @POST
    @Operation(summary = "Buat aplikasi baru (owner = user login)")
    public Response create(@Valid ApplicationRequest request) {
        ApplicationResponse response = ApplicationResponse.from(applicationService.create(request));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Operation(summary = "List aplikasi (milik sendiri; ADMIN melihat semua)")
    public List<ApplicationResponse> list() {
        return applicationService.list().stream().map(ApplicationResponse::from).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail 1 aplikasi")
    public ApplicationResponse get(@PathParam("id") Long id) {
        return ApplicationResponse.from(applicationService.getOwned(id));
    }

    @POST
    @Path("/{id}/update")
    @Operation(summary = "Update aplikasi")
    public ApplicationResponse update(@PathParam("id") Long id, @Valid ApplicationRequest request) {
        return ApplicationResponse.from(applicationService.update(id, request));
    }

    @POST
    @Path("/{id}/delete")
    @Operation(summary = "Hapus aplikasi")
    public Response delete(@PathParam("id") Long id) {
        applicationService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/checklist/generate")
    @Operation(summary = "Generate checklist item untuk aplikasi dari ChecklistMaster aktif (idempoten)")
    public List<ChecklistItemResponse> generateChecklist(@PathParam("id") Long id) {
        return checklistService.generateForApplication(id).stream()
                .map(ChecklistItemResponse::from).toList();
    }

    @GET
    @Path("/{id}/checklist")
    @Operation(summary = "List checklist item milik aplikasi")
    public List<ChecklistItemResponse> listChecklist(@PathParam("id") Long id) {
        return checklistService.listForApplication(id).stream()
                .map(ChecklistItemResponse::from).toList();
    }

    @POST
    @Path("/checklist/{itemId}/update")
    @Operation(summary = "Update status/keterangan 1 checklist item secara manual")
    public ChecklistItemResponse updateChecklistItem(@PathParam("itemId") Long itemId,
                                                     @Valid ChecklistItemUpdateRequest request) {
        return ChecklistItemResponse.from(checklistService.updateItem(itemId, request));
    }
}
