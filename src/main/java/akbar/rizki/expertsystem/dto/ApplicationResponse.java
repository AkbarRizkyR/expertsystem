package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.Application;

import java.time.OffsetDateTime;

public class ApplicationResponse {

    public Long id;
    public String name;
    public String description;
    public Long ownerId;
    public OffsetDateTime createdAt;

    public static ApplicationResponse from(Application app) {
        ApplicationResponse r = new ApplicationResponse();
        r.id = app.id;
        r.name = app.name;
        r.description = app.description;
        r.ownerId = app.ownerId;
        r.createdAt = app.createdAt;
        return r;
    }
}
