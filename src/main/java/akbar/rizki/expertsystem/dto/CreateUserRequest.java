package akbar.rizki.expertsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "name wajib diisi")
    @Size(max = 150, message = "name maksimal 150 karakter")
    public String name;

    @NotBlank(message = "email wajib diisi")
    @Email(message = "format email tidak valid")
    public String email;

    @NotBlank(message = "password wajib diisi")
    @Size(min = 8, message = "password minimal 8 karakter")
    public String password;

    @NotNull(message = "roleId wajib diisi (1=DEVELOPER, 2=SECURITY_REVIEWER, 3=ADMIN)")
    public Integer roleId;
}
