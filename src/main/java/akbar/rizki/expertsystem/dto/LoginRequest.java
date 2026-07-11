package akbar.rizki.expertsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "email wajib diisi")
    @Email(message = "format email tidak valid")
    public String email;

    @NotBlank(message = "password wajib diisi")
    public String password;
}
