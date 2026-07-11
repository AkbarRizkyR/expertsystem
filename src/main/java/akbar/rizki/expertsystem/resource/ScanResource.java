package akbar.rizki.expertsystem.resource;

import akbar.rizki.expertsystem.dto.ScanResponse;
import akbar.rizki.expertsystem.service.CurrentUser;
import akbar.rizki.expertsystem.service.ScanService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.temporal.ChronoUnit;

@Path("/api/scan")
public class ScanResource {

    @Inject
    ScanService scanService;

    @Inject
    CurrentUser currentUser;

    @POST
    @RolesAllowed({"DEVELOPER", "ADMIN"})
    @RateLimit(value = 20, window = 1, windowUnit = ChronoUnit.MINUTES)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload 1 file kode dan scan dengan Semgrep untuk 1 checklist item")
    public Response scan(
            @RestForm("applicationChecklistItemId") Long applicationChecklistItemId,
            @RestForm("frameworkId") Integer frameworkId,
            @RestForm("file") FileUpload file) {

        if (applicationChecklistItemId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("applicationChecklistItemId wajib diisi")
                    .build();
        }
        if (frameworkId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("frameworkId wajib diisi (untuk menentukan knowledge_rules mana yang dipakai)")
                    .build();
        }

        ScanResponse result = scanService.processScan(
                applicationChecklistItemId, frameworkId, currentUser.id(), file
        );

        return Response.ok(result).build();
    }
}
