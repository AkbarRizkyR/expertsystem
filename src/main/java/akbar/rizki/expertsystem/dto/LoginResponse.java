package akbar.rizki.expertsystem.dto;

public class LoginResponse {

    public String token;
    public String tokenType = "Bearer";
    public Long userId;
    public String name;
    public String role;

    public LoginResponse(String token, Long userId, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }
}
