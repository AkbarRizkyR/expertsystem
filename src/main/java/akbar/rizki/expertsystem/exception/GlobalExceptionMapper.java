package akbar.rizki.expertsystem.exception;

import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        // rate limit terlampaui -> 429 Too Many Requests
        if (exception instanceof RateLimitException) {
            return Response.status(429)
                    .entity(Map.of("error", "Terlalu banyak request, coba lagi beberapa saat lagi"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof WebApplicationException wae) {
            // request error yang sudah jelas (400, 401, 403, 404, dll) — teruskan apa adanya
            return Response.status(wae.getResponse().getStatus())
                .entity(Map.of("error", exception.getMessage() != null ? exception.getMessage() : "Request tidak valid"))
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

        // error tak terduga — jangan bocorkan stack trace ke client, cukup log di server
        LOG.error("Unhandled exception", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Map.of("error", "Terjadi kesalahan internal, ini salah di Backend"))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
