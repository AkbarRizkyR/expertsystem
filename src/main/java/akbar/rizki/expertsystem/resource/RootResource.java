package akbar.rizki.expertsystem.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootResource {

    @GET
    public Response redirectToSwagger() {
        return Response.seeOther(URI.create("/swagger-ui")).build();
    }
}
