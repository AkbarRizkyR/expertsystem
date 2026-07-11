package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.User;

import java.time.OffsetDateTime;

public class UserResponse {

    public Long id;
    public String name;
    public String email;
    public String role;
    public OffsetDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.id;
        r.name = user.name;
        r.email = user.email;
        r.role = user.role != null ? user.role.name : null;
        r.createdAt = user.createdAt;
        return r;
    }
}
