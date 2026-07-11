package akbar.rizki.expertsystem.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            // ambil segmen terakhir dari path (mis. "login.arg0.email" -> "email")
            int lastDot = path.lastIndexOf('.');
            String field = lastDot >= 0 ? path.substring(lastDot + 1) : path;
            fieldErrors.put(field, violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Validasi gagal", "fields", fieldErrors))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
